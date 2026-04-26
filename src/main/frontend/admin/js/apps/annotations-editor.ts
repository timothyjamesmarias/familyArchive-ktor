/**
 * Annotations Editor
 * Allows admins to add, edit, and delete annotations on photos
 */

import { Modal } from '../components/Modal';
import { AnnotationEditorStateManager } from '../annotations/AnnotationEditorState';
import type { ArtifactFile, Annotation } from '../annotations/types';
import { saveAnnotations as saveAnnotationsApi } from '../annotations/api';

// Import web components (they self-register)
import { AnnotationMarker } from '../annotations/components/AnnotationMarker';
import { ContextMenu } from '../annotations/components/ContextMenu';
import { AnnotationForm } from '../annotations/components/AnnotationForm';

export function initializeAnnotationsEditor(): void {
  const root = document.getElementById('annotations-editor-root');

  if (!root) {
    return;
  }

  const artifactId = root.dataset.artifactId;
  const filesJson = root.dataset.files;

  if (!filesJson || !artifactId) {
    console.warn('Missing required data attributes for annotations editor');
    return;
  }

  // Parse files data
  let files: ArtifactFile[];
  try {
    files = JSON.parse(filesJson);
  } catch (e) {
    console.error('Failed to parse files data:', e);
    return;
  }

  // Initialize state manager
  const state = new AnnotationEditorStateManager();
  state.initialize(files);

  // Create modal for editing
  const modal = new Modal('annotations-modal');

  // Attach click handlers to gallery items
  const galleryItems = root.querySelectorAll('[data-image-gallery-item]');
  galleryItems.forEach((item) => {
    const fileId = parseInt((item as HTMLElement).dataset.fileId || '0');
    if (!fileId) return;

    item.addEventListener('click', () => {
      openImageEditor(fileId, state, modal);
    });
  });

  console.log('Annotations editor initialized', {
    artifactId,
    filesCount: files.length,
  });
}

/**
 * Open the image editor modal for a specific file
 */
function openImageEditor(fileId: number, state: AnnotationEditorStateManager, modal: Modal): void {
  // Set current file in state
  state.setCurrentFile(fileId);

  const file = state.getCurrentFile();
  if (!file) {
    console.error('File not found:', fileId);
    return;
  }

  // Create modal content
  const modalContent = document.createElement('div');
  modalContent.className = 'flex flex-col h-full';

  // Image container with relative positioning for markers
  const imageWrapper = document.createElement('div');
  imageWrapper.className =
    'flex-1 flex items-center justify-center bg-gray-100 dark:bg-gray-900 rounded-lg p-4';

  const imageContainer = document.createElement('div');
  imageContainer.className = 'relative inline-block';
  imageContainer.dataset.imageContainer = 'true';

  const image = document.createElement('img');
  image.src = `/uploads/${file.storagePath}`;
  image.alt = `File ${file.fileSequence}`;
  image.className = 'max-w-full max-h-[60vh] object-contain cursor-crosshair';
  image.dataset.annotationImage = 'true';

  imageContainer.appendChild(image);
  imageWrapper.appendChild(imageContainer);
  modalContent.appendChild(imageWrapper);

  // Create web component instances
  const contextMenu = new ContextMenu();
  const annotationForm = new AnnotationForm();

  imageContainer.appendChild(contextMenu);
  imageContainer.appendChild(annotationForm);

  // Render existing markers
  const markers = new Map<number, AnnotationMarker>();
  renderMarkers(imageContainer, state.getCurrentAnnotations(), markers);

  // Handle image click - show context menu
  image.addEventListener('click', (e) => {
    e.stopPropagation();

    const rect = image.getBoundingClientRect();
    const xCoord = (e.clientX - rect.left) / rect.width;
    const yCoord = (e.clientY - rect.top) / rect.height;

    // Show context menu at click position
    contextMenu.show(e.clientX, e.clientY, xCoord, yCoord);
  });

  // Handle context menu - add annotation
  contextMenu.addEventListener('add-annotation', ((e: CustomEvent) => {
    const { xCoord, yCoord } = e.detail;
    // Show form to enter annotation text
    annotationForm.showCreate(xCoord, yCoord, e.detail.clientX || 0, e.detail.clientY || 0);
  }) as EventListener);

  // Handle annotation creation
  annotationForm.addEventListener('annotation-create', ((e: CustomEvent) => {
    const annotation: Annotation = e.detail.annotation;
    // Add to state
    state.addAnnotation(fileId, annotation);

    // Re-render markers
    renderMarkers(imageContainer, state.getCurrentAnnotations(), markers);
  }) as EventListener);

  // Handle annotation update
  annotationForm.addEventListener('annotation-update', ((e: CustomEvent) => {
    const annotation: Annotation = e.detail.annotation;
    if (annotation.id) {
      state.updateAnnotation(fileId, annotation.id, annotation);
      // Re-render markers
      renderMarkers(imageContainer, state.getCurrentAnnotations(), markers);
    }
  }) as EventListener);

  // Handle annotation deletion
  annotationForm.addEventListener('annotation-delete', ((e: CustomEvent) => {
    const annotationId: number = e.detail.annotationId;
    state.deleteAnnotation(fileId, annotationId);
    // Re-render markers
    renderMarkers(imageContainer, state.getCurrentAnnotations(), markers);
  }) as EventListener);

  // Handle marker clicks - show edit form
  imageContainer.addEventListener('marker-click', ((e: CustomEvent) => {
    const annotation: Annotation = e.detail.annotation;
    const marker = markers.get(annotation.id || 0);
    if (marker) {
      const rect = marker.getBoundingClientRect();
      annotationForm.showEdit(annotation, rect.left, rect.bottom + 10);
    }
  }) as EventListener);

  // Footer with save/cancel buttons
  const footer = document.createElement('div');
  footer.className = 'flex justify-end gap-2';

  const cancelButton = document.createElement('button');
  cancelButton.type = 'button';
  cancelButton.className =
    'px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors';
  cancelButton.textContent = 'Cancel';
  cancelButton.addEventListener('click', () => {
    // Check for unsaved changes
    if (state.hasChanges(fileId)) {
      if (confirm('You have unsaved changes. Are you sure you want to close?')) {
        state.resetChanges(fileId);
        modal.close();
      }
    } else {
      modal.close();
    }
  });

  const saveButton = document.createElement('button');
  saveButton.type = 'button';
  saveButton.className =
    'px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors';
  saveButton.textContent = 'Save';
  saveButton.addEventListener('click', () => {
    saveAnnotations(fileId, state, modal);
  });

  footer.appendChild(cancelButton);
  footer.appendChild(saveButton);

  // Open modal
  modal.open({
    title: `Edit Annotations - File ${file.fileSequence}`,
    content: modalContent,
    footer: footer,
  });

  // Handle modal close
  modal.onClose(() => {
    state.clearCurrentFile();
  });
}

/**
 * Render annotation markers on the image
 */
function renderMarkers(
  container: HTMLElement,
  annotations: Annotation[],
  markers: Map<number, AnnotationMarker>
): void {
  // Remove all existing markers
  markers.forEach((marker) => marker.destroy());
  markers.clear();

  // Render new markers
  annotations.forEach((annotation) => {
    const marker = new AnnotationMarker();
    marker.setAnnotation(annotation);

    if (annotation.id) {
      markers.set(annotation.id, marker);
    }

    container.appendChild(marker);
  });
}

/**
 * Save annotations for a file
 */
async function saveAnnotations(
  fileId: number,
  state: AnnotationEditorStateManager,
  modal: Modal
): Promise<void> {
  const annotations = state.getAnnotations(fileId);

  try {
    // Show loading state
    const saveButton = document.querySelector('[data-modal-footer] button:last-child');
    if (saveButton) {
      saveButton.textContent = 'Saving...';
      (saveButton as HTMLButtonElement).disabled = true;
    }

    // Call API to save annotations
    const savedAnnotations = await saveAnnotationsApi(fileId, annotations);

    // Mark as saved in state
    state.markSaved(fileId, savedAnnotations);

    // Close modal
    modal.close();

    // Show success message (could be a toast notification instead)
    console.log('Annotations saved successfully');
  } catch (error) {
    console.error('Failed to save annotations:', error);
    alert(
      `Failed to save annotations: ${error instanceof Error ? error.message : 'Unknown error'}`
    );

    // Re-enable save button
    const saveButton = document.querySelector('[data-modal-footer] button:last-child');
    if (saveButton) {
      saveButton.textContent = 'Save';
      (saveButton as HTMLButtonElement).disabled = false;
    }
  }
}
