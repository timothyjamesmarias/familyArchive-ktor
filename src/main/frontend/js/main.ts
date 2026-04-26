// Main application entry point
import './shared/darkMode';
import './shared/mobileMenu';
import {
  initializePhotoGalleries,
  initializeAnnotatedPhotos,
} from '../admin/js/components/artifacts/photos';

console.log('Family Archive - Frontend initialized');

// Example: Initialize common functionality
document.addEventListener('DOMContentLoaded', () => {
  console.log('DOM fully loaded');

  // Initialize photo galleries and annotated photos
  initializePhotoGalleries();
  initializeAnnotatedPhotos();
});

// Export utilities for use in other modules
export const utils = {
  formatDate: (date: Date) => {
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    }).format(date);
  },
};
