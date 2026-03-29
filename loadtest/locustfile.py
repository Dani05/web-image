import os
import random
import string
import time
from typing import Any

from locust import HttpUser, between, task


API_PREFIX = "/api"
REGISTER_PATH = f"{API_PREFIX}/profile/register"
LOGIN_PATH = f"{API_PREFIX}/profile/login"
IMAGES_PATH = f"{API_PREFIX}/images"

PASSWORD = os.getenv("LOADTEST_PASSWORD", "LoadTest123!")
USERNAME_PREFIX = os.getenv("LOADTEST_USERNAME_PREFIX", "loaduser")
USER_INDEX_MOD = max(1, int(os.getenv("LOADTEST_USER_INDEX_MOD", "5000")))

# Read-heavy mix with write pressure for autoscaling tests.
TASK_WEIGHTS = {
    "list_images": int(os.getenv("WEIGHT_LIST", "6")),
    "get_image": int(os.getenv("WEIGHT_GET", "4")),
    "upload_image": int(os.getenv("WEIGHT_UPLOAD", "5")),
    "update_image": int(os.getenv("WEIGHT_UPDATE", "2")),
    "delete_image": int(os.getenv("WEIGHT_DELETE", "2")),
}


def _rand_suffix(length: int = 8) -> str:
    chars = string.ascii_lowercase + string.digits
    return "".join(random.choice(chars) for _ in range(length))


class PhotoAlbumUser(HttpUser):
    wait_time = between(
        float(os.getenv("WAIT_MIN_SECONDS", "0.2")),
        float(os.getenv("WAIT_MAX_SECONDS", "1.2")),
    )

    def on_start(self) -> None:
        self.username = self._build_username()
        self.password = PASSWORD
        self.token = None
        self.owned_image_ids: list[int] = []
        self._ensure_user_and_login()

    def _build_username(self) -> str:
        idx = (hash(f"{id(self)}-{time.time_ns()}") % USER_INDEX_MOD) + 1
        return f"{USERNAME_PREFIX}-{idx:04d}"

    def _ensure_user_and_login(self) -> None:
        payload = {"username": self.username, "password": self.password}
        self.client.post(REGISTER_PATH, json=payload, name="auth_register")

        with self.client.post(
            LOGIN_PATH,
            json=payload,
            name="auth_login",
            catch_response=True,
        ) as response:
            if response.status_code != 200:
                response.failure(f"Login failed with status={response.status_code}")
                return

            token = response.text.strip()
            if not token:
                response.failure("Login returned empty token")
                return

            self.token = token
            response.success()

    def _auth_headers(self) -> dict[str, str]:
        if not self.token:
            return {}
        return {"Authorization": f"Bearer {self.token}"}

    def _extract_ids(self, body: Any) -> list[int]:
        ids: list[int] = []
        if isinstance(body, list):
            for item in body:
                if isinstance(item, dict) and isinstance(item.get("id"), int):
                    ids.append(item["id"])
        return ids

    @task(TASK_WEIGHTS["list_images"])
    def list_images(self) -> None:
        with self.client.get(IMAGES_PATH, name="images_list", catch_response=True) as response:
            if response.status_code != 200:
                response.failure(f"List failed with status={response.status_code}")
                return
            try:
                body = response.json()
                ids = self._extract_ids(body)
                if ids:
                    # Keep a small rolling cache of IDs per user for get/update/delete tasks.
                    self.owned_image_ids = (self.owned_image_ids + ids)[-50:]
                response.success()
            except Exception as exc:
                response.failure(f"Invalid JSON in list: {exc}")

    @task(TASK_WEIGHTS["get_image"])
    def get_image(self) -> None:
        image_id = self._pick_known_image_id()
        if image_id is None:
            self.list_images()
            image_id = self._pick_known_image_id()
            if image_id is None:
                return

        with self.client.get(
            f"{IMAGES_PATH}/{image_id}",
            name="images_get",
            catch_response=True,
        ) as response:
            if response.status_code == 200:
                response.success()
            elif response.status_code == 404:
                response.failure("Image disappeared before read")
            else:
                response.failure(f"Get failed with status={response.status_code}")

    @task(TASK_WEIGHTS["upload_image"])
    def upload_image(self) -> None:
        if not self.token:
            return

        img_name = f"lt-{_rand_suffix(6)}"
        desc = f"loadtest-{_rand_suffix(10)}"
        file_content = os.urandom(8 * 1024)
        files = {
            "file": ("loadtest.bin", file_content, "application/octet-stream"),
        }
        data = {
            "name": img_name,
            "description": desc,
        }

        with self.client.post(
            IMAGES_PATH,
            headers=self._auth_headers(),
            files=files,
            data=data,
            name="images_upload",
            catch_response=True,
        ) as response:
            if response.status_code == 201:
                response.success()
                # Refresh IDs after successful upload.
                self._refresh_ids_after_write()
            else:
                response.failure(f"Upload failed with status={response.status_code}")

    @task(TASK_WEIGHTS["update_image"])
    def update_image(self) -> None:
        image_id = self._pick_known_image_id()
        if image_id is None:
            return

        payload = {
            "id": image_id,
            "name": f"updated-{_rand_suffix(5)}",
            "url": "loadtest://placeholder",
            "description": f"updated-by-locust-{_rand_suffix(8)}",
        }

        with self.client.put(
            f"{IMAGES_PATH}/{image_id}",
            json=payload,
            headers=self._auth_headers(),
            name="images_update",
            catch_response=True,
        ) as response:
            if response.status_code == 200:
                response.success()
            elif response.status_code == 404:
                response.failure("Update target not found")
            else:
                response.failure(f"Update failed with status={response.status_code}")

    @task(TASK_WEIGHTS["delete_image"])
    def delete_image(self) -> None:
        if not self.token:
            return

        image_id = self._pick_known_image_id()
        if image_id is None:
            return

        with self.client.delete(
            f"{IMAGES_PATH}/{image_id}",
            headers=self._auth_headers(),
            name="images_delete",
            catch_response=True,
        ) as response:
            if response.status_code == 204:
                self.owned_image_ids = [x for x in self.owned_image_ids if x != image_id]
                response.success()
            elif response.status_code in (401, 402, 404):
                response.failure(f"Delete rejected with status={response.status_code}")
            else:
                response.failure(f"Delete failed with status={response.status_code}")

    def _pick_known_image_id(self) -> int | None:
        if not self.owned_image_ids:
            return None
        return random.choice(self.owned_image_ids)

    def _refresh_ids_after_write(self) -> None:
        response = self.client.get(IMAGES_PATH, name="images_list_after_write")
        if response.status_code != 200:
            return
        try:
            ids = self._extract_ids(response.json())
            if ids:
                self.owned_image_ids = (self.owned_image_ids + ids)[-50:]
        except Exception:
            return

