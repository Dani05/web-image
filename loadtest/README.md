# Cloud-to-cloud load test with Locust on OpenShift

This folder contains a runnable load test app for the backend API.

## What is covered

The script calls the main API flows:

- `POST /api/profile/register`
- `POST /api/profile/login`
- `GET /api/images`
- `GET /api/images/{id}`
- `POST /api/images` (multipart upload)
- `PUT /api/images/{id}`
- `DELETE /api/images/{id}`

## Files

- `loadtest/locustfile.py`: scenario and request mix
- `loadtest/requirements.txt`: Python dependencies
- `loadtest/Dockerfile`: container image for OpenShift
- `openshift/loadtest-configmap.yaml`: runtime knobs
- `openshift/loadtest-job.yaml`: headless batch run
- `openshift/loadtest-ui.yaml`: optional Locust web UI route

## Quick local smoke check

```bash
cd /Users/bartadaniel/Documents/BME/felho_labor/web-image/loadtest
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
TARGET_HOST="https://your-backend-route.example.com" locust -f locustfile.py --headless --host "$TARGET_HOST" --users 5 --spawn-rate 1 --run-time 1m --only-summary
```

## Build and push image

Set your own image name, then build and push:

```bash
cd /Users/bartadaniel/Documents/BME/felho_labor/web-image/loadtest
docker build -t ghcr.io/your-org/web-image-locust:latest .
docker push ghcr.io/your-org/web-image-locust:latest
```

## Deploy to OpenShift (headless test run)

1) Update image in `openshift/loadtest-job.yaml` and `openshift/loadtest-ui.yaml`.
2) Update target backend URL in `openshift/loadtest-configmap.yaml`.
3) Apply manifests:

```bash
cd /Users/bartadaniel/Documents/BME/felho_labor/web-image
oc apply -f openshift/loadtest-configmap.yaml
oc apply -f openshift/loadtest-job.yaml
```

4) Watch and fetch output:

```bash
oc get jobs
oc logs -f job/locust-loadtest-headless
```

## Optional: interactive Locust UI in OpenShift

```bash
cd /Users/bartadaniel/Documents/BME/felho_labor/web-image
oc apply -f openshift/loadtest-configmap.yaml
oc apply -f openshift/loadtest-ui.yaml
oc get route locust-ui
```

Open the route URL, then start tests from UI.

## Good usage pattern for autoscaling verification

Run in 4 phases and keep timestamps:

1. Baseline: low users (`10`, `5m`)
2. Ramp: increase (`40 -> 80 -> 120`)
3. Peak hold: hold high load (`15-20m`)
4. Cooldown: drop to low (`5-10m`) and verify scale-in

Tune these ConfigMap values for each run:

- `LOCUST_USERS`
- `LOCUST_SPAWN_RATE`
- `LOCUST_RUN_TIME`
- Task weights (`WEIGHT_*`)

## Notes

- `TARGET_HOST` must be the backend cloud route URL.
- Register may return non-200 if user already exists; this is expected for repeated runs.
- Delete can return `402` on ownership mismatch in the current backend implementation.

## Troubleshooting

If you see errors like `Runner.__init__.<locals>.on_request() missing ...`, your environment is likely using an older Locust version with Python 3.13.

Recreate the venv and reinstall from `requirements.txt`:

```bash
cd /Users/bartadaniel/Documents/BME/felho_labor/web-image/loadtest
rm -rf .venv
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
locust --version
```

You should see `locust 2.43.3` (or newer if you intentionally change the pin).
