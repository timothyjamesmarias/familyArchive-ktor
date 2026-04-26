/**
 * Admin panel entry point
 *
 * This module initializes all admin-specific functionality.
 */

import { initializeUppyUploaders } from './components/UppyUploader';
import { initializeThumbnailBackfill } from './components/ThumbnailBackfill';
import { initializeTinyMCEEditors } from './components/TinyMCEEditor';
import { initializeModals } from './components/Modal';
import { attachFormConfirmation } from './utils/formConfirmation';
import { initializeAnnotationsEditor } from './apps/annotations-editor';

// Import dark mode from shared (shared between admin and public)
import darkMode from '../../js/shared/darkMode';

// Make dark mode available globally for admin layout
window.darkMode = darkMode;

/**
 * Initialize sidebar toggle
 */
function initializeSidebar(): void {
  const mobileSidebarToggle = document.getElementById('mobile-sidebar-toggle');
  const sidebarCloseButton = document.getElementById('sidebar-toggle');
  const sidebar = document.getElementById('admin-sidebar');
  const overlay = document.getElementById('sidebar-overlay');

  // Toggle sidebar on mobile (from header button)
  if (mobileSidebarToggle && sidebar && overlay) {
    mobileSidebarToggle.addEventListener('click', () => {
      sidebar.classList.toggle('-translate-x-full');
      sidebar.classList.toggle('translate-x-0');
      overlay.classList.toggle('hidden');
    });
  }

  // Close sidebar button (inside sidebar on mobile)
  if (sidebarCloseButton && sidebar && overlay) {
    sidebarCloseButton.addEventListener('click', () => {
      sidebar.classList.add('-translate-x-full');
      sidebar.classList.remove('translate-x-0');
      overlay.classList.add('hidden');
    });
  }

  // Close sidebar when clicking overlay
  if (overlay && sidebar) {
    overlay.addEventListener('click', () => {
      sidebar.classList.add('-translate-x-full');
      sidebar.classList.remove('translate-x-0');
      overlay.classList.add('hidden');
    });
  }
}

/**
 * Initialize form confirmations for edit pages
 */
function initializeFormConfirmations(): void {
  // Family edit form confirmation
  attachFormConfirmation({
    formId: 'familyEditForm',
    message:
      'Are you sure you want to update this family? This will affect all associated family members.',
  });

  // Individual edit form confirmation
  attachFormConfirmation({
    formId: 'individualEditForm',
    message:
      'Are you sure you want to update this individual? This will affect their family tree relationships.',
  });
}

/**
 * Initialize all admin components
 */
function initializeAdmin(): void {
  console.log('Admin panel initialized');

  // Initialize Uppy uploaders
  initializeUppyUploaders();

  // Initialize thumbnail backfill
  initializeThumbnailBackfill();

  // Initialize TinyMCE editors
  initializeTinyMCEEditors();

  // Initialize modals
  initializeModals();

  // Initialize sidebar
  initializeSidebar();

  // Initialize form confirmations
  initializeFormConfirmations();

  // Initialize annotations editor
  initializeAnnotationsEditor();
}

// Initialize when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initializeAdmin);
} else {
  initializeAdmin();
}
