import React, { useState, useEffect } from 'react';
import Header from '../components/Header';
import { getAllImages } from '../services/api';
import './HomePage.css';

const HomePage = () => {
    const [images, setImages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selected, setSelected] = useState(null);

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
                    <div className="image-grid">
                        {images.map((image) => (
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

