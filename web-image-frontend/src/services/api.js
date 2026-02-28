const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

// Auth API calls
export const register = async (username, password) => {
    const response = await fetch(`${API_BASE_URL}/profile/register`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
    });

    if (!response.ok) {
        const error = await response.text();
        throw new Error(error || 'Registration failed');
    }

    return await response.text();
};

export const login = async (username, password) => {
    const response = await fetch(`${API_BASE_URL}/profile/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
    });

    if (!response.ok) {
        const error = await response.text();
        throw new Error(error || 'Login failed');
    }

    return await response.text(); // Returns JWT token
};

// Image API calls
export const getAllImages = async () => {
    const response = await fetch(`${API_BASE_URL}/images`);

    if (!response.ok) {
        throw new Error('Failed to fetch images');
    }

    return await response.json();
};

export const getImageById = async (id) => {
    const response = await fetch(`${API_BASE_URL}/images/${id}`);

    if (!response.ok) {
        throw new Error('Failed to fetch image');
    }

    return await response.json();
};

export const createImage = async (image, token) => {
    const response = await fetch(`${API_BASE_URL}/images`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(image),
    });

    if (!response.ok) {
        throw new Error('Failed to create image');
    }

    return await response.json();
};

export const deleteImage = async (id, token) => {
    const response = await fetch(`${API_BASE_URL}/images/${id}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${token}`,
        },
    });

    if (!response.ok) {
        throw new Error('Failed to delete image');
    }
};

