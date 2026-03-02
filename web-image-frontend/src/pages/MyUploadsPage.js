import React, { useState, useEffect, useRef } from 'react';
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
    const [file, setFile] = useState(null);
    const [preview, setPreview] = useState(null);
    const [dragActive, setDragActive] = useState(false);
    const [description, setDescription] = useState('');
    const fileInputRef = useRef(null);

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

    const handleFileSelect = (selectedFile) => {
        if (selectedFile && selectedFile.type.startsWith('image/')) {
            setFile(selectedFile);
            setPreview(URL.createObjectURL(selectedFile));
            setError(null);
        } else {
            setError('Please select a valid image file');
        }
    };

    const handleDrag = (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (e.type === 'dragenter' || e.type === 'dragover') {
            setDragActive(true);
        } else if (e.type === 'dragleave') {
            setDragActive(false);
        }
    };

    const handleDrop = (e) => {
        e.preventDefault();
        e.stopPropagation();
        setDragActive(false);
        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            handleFileSelect(e.dataTransfer.files[0]);
        }
    };

    const handleFileInputChange = (e) => {
        if (e.target.files && e.target.files[0]) {
            handleFileSelect(e.target.files[0]);
        }
    };

    const removeFile = () => {
        setFile(null);
        if (preview) {
            URL.revokeObjectURL(preview);
        }
        setPreview(null);
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }
    };

    const handleUpload = async (e) => {
        e.preventDefault();

        if (!name || !file) {
            setError('Name and image file are required');
            return;
        }

        try {
            setUploading(true);
            setError(null);

            await createImage(file, name, description || '', token);

            // Clear form
            setName('');
            removeFile();
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
                            <label>Image File *</label>
                            <div
                                className={`drop-zone ${dragActive ? 'drop-zone--active' : ''} ${file ? 'drop-zone--has-file' : ''}`}
                                onDragEnter={handleDrag}
                                onDragLeave={handleDrag}
                                onDragOver={handleDrag}
                                onDrop={handleDrop}
                                onClick={() => !uploading && fileInputRef.current?.click()}
                            >
                                <input
                                    ref={fileInputRef}
                                    type="file"
                                    accept="image/*"
                                    onChange={handleFileInputChange}
                                    disabled={uploading}
                                    style={{ display: 'none' }}
                                />
                                {file && preview ? (
                                    <div className="drop-zone__preview">
                                        <img src={preview} alt="Preview" className="drop-zone__thumb" />
                                        <div className="drop-zone__file-info">
                                            <span className="drop-zone__filename">{file.name}</span>
                                            <span className="drop-zone__filesize">
                                                {(file.size / 1024 / 1024).toFixed(2)} MB
                                            </span>
                                            <button
                                                type="button"
                                                className="btn btn-danger btn-small"
                                                onClick={(e) => { e.stopPropagation(); removeFile(); }}
                                                disabled={uploading}
                                            >
                                                Remove
                                            </button>
                                        </div>
                                    </div>
                                ) : (
                                    <div className="drop-zone__prompt">
                                        <span className="drop-zone__icon">📁</span>
                                        <p>Drag & drop an image here or <strong>click to browse</strong></p>
                                        <span className="drop-zone__hint">Supports JPG, PNG, GIF, WebP</span>
                                    </div>
                                )}
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

