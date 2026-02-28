import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import { useAuth } from '../context/AuthContext';
import { getAllImages, createImage, deleteImage } from '../services/api';
import './MyUploadsPage.css';

const MyUploadsPage = () => {
    const [images, setImages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [uploading, setUploading] = useState(false);

    const [name, setName] = useState('');
    const [url, setUrl] = useState('');
    const [description, setDescription] = useState('');

    const { isAuthenticated, token } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        if (!isAuthenticated()) {
            navigate('/login');
            return;
        }
        loadMyImages();
    }, [isAuthenticated, navigate]);

    const loadMyImages = async () => {
        try {
            setLoading(true);
            const data = await getAllImages();
            // In a real app, filter by current user. For now, show all.
            setImages(data);
            setError(null);
        } catch (err) {
            setError('Failed to load images');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleUpload = async (e) => {
        e.preventDefault();

        if (!name || !url) {
            setError('Name and URL are required');
            return;
        }

        try {
            setUploading(true);
            setError(null);

            const newImage = {
                id: null,
                name,
                url,
                description: description || '',
            };

            await createImage(newImage, token);

            // Clear form
            setName('');
            setUrl('');
            setDescription('');

            // Reload images
            await loadMyImages();
        } catch (err) {
            setError(err.message || 'Failed to upload image');
        } finally {
            setUploading(false);
        }
    };

    const handleDelete = async (imageId) => {
        if (!window.confirm('Are you sure you want to delete this image?')) {
            return;
        }

        try {
            await deleteImage(imageId, token);
            await loadMyImages();
        } catch (err) {
            setError(err.message || 'Failed to delete image');
        }
    };

    if (!isAuthenticated()) {
        return null;
    }

    return (
        <div className="page">
            <Header />
            <main className="main-content">
                <h2>My Uploads</h2>

                <div className="upload-form-container">
                    <h3>Upload New Image</h3>
                    <form onSubmit={handleUpload} className="upload-form">
                        {error && <div className="error-message">{error}</div>}

                        <div className="form-row">
                            <div className="form-group">
                                <label htmlFor="name">Image Name *</label>
                                <input
                                    type="text"
                                    id="name"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    placeholder="Enter image name"
                                    disabled={uploading}
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="url">Image URL *</label>
                                <input
                                    type="url"
                                    id="url"
                                    value={url}
                                    onChange={(e) => setUrl(e.target.value)}
                                    placeholder="https://example.com/image.jpg"
                                    disabled={uploading}
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label htmlFor="description">Description</label>
                            <textarea
                                id="description"
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                placeholder="Enter image description (optional)"
                                disabled={uploading}
                                rows="3"
                            />
                        </div>

                        <button type="submit" className="btn btn-primary" disabled={uploading}>
                            {uploading ? 'Uploading...' : 'Upload Image'}
                        </button>
                    </form>
                </div>

                <div className="my-images-section">
                    <h3>Your Images</h3>

                    {loading && <p className="message">Loading images...</p>}

                    {!loading && images.length === 0 && (
                        <p className="message">You haven't uploaded any images yet.</p>
                    )}

                    {!loading && images.length > 0 && (
                        <div className="image-grid">
                            {images.map((image) => (
                                <div key={image.id} className="image-card">
                                    <img src={image.url} alt={image.name} />
                                    <div className="image-info">
                                        <h4>{image.name}</h4>
                                        <p>{image.description}</p>
                                        <button
                                            onClick={() => handleDelete(image.id)}
                                            className="btn btn-danger btn-small"
                                        >
                                            Delete
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
};

export default MyUploadsPage;

