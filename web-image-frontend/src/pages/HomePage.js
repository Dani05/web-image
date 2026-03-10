import React, { useState, useEffect } from 'react';
import Header from '../components/Header';
import { getAllImages } from '../services/api';
import './HomePage.css';

const HomePage = () => {
    const [images, setImages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selected, setSelected] = useState(null);
    const [sortBy, setSortBy] = useState('date');
    const [sortDir, setSortDir] = useState('desc');

    const sortedImages = [...images].sort((a, b) => {
        if (sortBy === 'name') {
            return sortDir === 'asc'
                ? a.name.localeCompare(b.name)
                : b.name.localeCompare(a.name);
        } else {
            const dateA = a.uploadedAt ? new Date(a.uploadedAt) : 0;
            const dateB = b.uploadedAt ? new Date(b.uploadedAt) : 0;
            return sortDir === 'asc' ? dateA - dateB : dateB - dateA;
        }
    });

    const handleSort = (field) => {
        if (sortBy === field) {
            setSortDir(prev => prev === 'asc' ? 'desc' : 'asc');
        } else {
            setSortBy(field);
            setSortDir('asc');
        }
    };

    useEffect(() => {
        loadImages();
    }, []);

    const loadImages = async () => {
        try {
            setLoading(true);
            const data = await getAllImages();
            setImages(data);
            setError(null);
        } catch (err) {
            setError('Failed to load images');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="page">
            <Header />
            <main className="main-content">
                <h2>Image Gallery</h2>

                {loading && <p className="message">Loading images...</p>}
                {error && <p className="error-message">{error}</p>}

                {!loading && !error && images.length === 0 && (
                    <p className="message">No images available yet. Be the first to upload!</p>
                )}

                {!loading && images.length > 0 && (
                    <>
                        <div className="sort-controls">
                            <span>Sort by:</span>
                            <button
                                className={`sort-btn${sortBy === 'name' ? ' active' : ''}`}
                                onClick={() => handleSort('name')}
                            >
                                Name {sortBy === 'name' ? (sortDir === 'asc' ? '↑' : '↓') : ''}
                            </button>
                            <button
                                className={`sort-btn${sortBy === 'date' ? ' active' : ''}`}
                                onClick={() => handleSort('date')}
                            >
                                Date {sortBy === 'date' ? (sortDir === 'asc' ? '↑' : '↓') : ''}
                            </button>
                        </div>
                        <div className="image-grid">
                            {sortedImages.map((image) => (
                                <div key={image.id} className="image-card" onClick={() => setSelected(image)} style={{ cursor: 'pointer' }}>
                                    <img src={image.imageData} alt={image.name} />
                                    <div className="image-info">
                                        <h3>{image.name}</h3>
                                        <p className="image-uploader">by {image.username}</p>
                                        <p>{image.description}</p>
                                        {image.uploadedAt && (
                                            <p className="upload-date">
                                                Uploaded: {new Date(image.uploadedAt).toLocaleString()}
                                            </p>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </>
                )}

            {selected && (
                <div className="lightbox" onClick={() => setSelected(null)}>
                    <div className="lightbox-content" onClick={(e) => e.stopPropagation()}>
                        <img src={selected.imageData} alt={selected.name} />
                        <div className="lightbox-info">
                            <h3>{selected.name}</h3>
                            <p className="image-uploader">by {selected.username}</p>
                            {selected.description && <p>{selected.description}</p>}
                            {selected.uploadedAt && (
                                <p className="upload-date">Uploaded: {new Date(selected.uploadedAt).toLocaleString()}</p>
                            )}
                        </div>
                        <button className="lightbox-close" onClick={() => setSelected(null)}>✕</button>
                    </div>
                </div>
            )}
            </main>
        </div>
    );
};

export default HomePage;

