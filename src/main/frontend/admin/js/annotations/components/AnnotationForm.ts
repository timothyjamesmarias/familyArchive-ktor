/**
 * Annotation Form Web Component
 * Form for adding or editing annotation text
 */

import type { Annotation } from '../types';

// Constants for positioning
const FORM_OFFSET = 10;
const VIEWPORT_PADDING = 20;

export class AnnotationForm extends HTMLElement {
  private formElement: HTMLDivElement | null = null;
  private textInput: HTMLInputElement | null = null;
  private annotation: Annotation | null = null;
  private mode: 'create' | 'edit' = 'create';

  constructor() {
    super();
  }

  connectedCallback(): void {
    this.render();
    this.hide();
  }

  /**
   * Show form for creating a new annotation
   */
  showCreate(xCoord: number, yCoord: number, clientX: number, clientY: number): void {
    this.mode = 'create';
    this.annotation = {
      annotationText: '',
      xCoord,
      yCoord,
    };
    this.render(); // Re-render to update mode-specific UI
    this.show(clientX, clientY);
  }

  /**
   * Show form for editing an existing annotation
   */
  showEdit(annotation: Annotation, clientX: number, clientY: number): void {
    this.mode = 'edit';
    this.annotation = annotation;
    this.render(); // Re-render to update mode-specific UI
    if (this.textInput) {
      this.textInput.value = annotation.annotationText;
    }
    this.show(clientX, clientY);
  }

  /**
   * Show the form at specific coordinates
   */
  private show(clientX: number, clientY: number): void {
    if (!this.formElement) return;

    this.formElement.classList.remove('hidden');

    const position = this.calculateFormPosition(clientX, clientY);
    this.applyPosition(position);
    this.focusInput();
    this.setupOutsideClickHandler();
  }

  /**
   * Calculate the optimal position for the form
   */
  private calculateFormPosition(clientX: number, clientY: number): { left: number; top: number } {
    if (!this.formElement) return { left: 0, top: 0 };

    const formRect = this.formElement.getBoundingClientRect();
    const initialPosition = this.getInitialPosition(clientX, clientY, formRect);

    return {
      left: this.constrainHorizontal(initialPosition.left, formRect.width),
      top: this.constrainVertical(initialPosition.top, clientY, formRect.height),
    };
  }

  /**
   * Get initial position based on mode
   */
  private getInitialPosition(
    clientX: number,
    clientY: number,
    formRect: DOMRect
  ): { left: number; top: number } {
    if (this.mode === 'edit') {
      return {
        left: clientX - formRect.width / 2,
        top: clientY + FORM_OFFSET,
      };
    }

    return {
      left: clientX + FORM_OFFSET,
      top: clientY + FORM_OFFSET,
    };
  }

  /**
   * Constrain horizontal position to viewport
   */
  private constrainHorizontal(left: number, formWidth: number): number {
    const viewportWidth = window.innerWidth;
    const maxLeft = viewportWidth - formWidth - VIEWPORT_PADDING;
    const minLeft = VIEWPORT_PADDING;

    return Math.max(minLeft, Math.min(left, maxLeft));
  }

  /**
   * Constrain vertical position to viewport
   */
  private constrainVertical(top: number, clientY: number, formHeight: number): number {
    const viewportHeight = window.innerHeight;
    const maxTop = viewportHeight - formHeight - VIEWPORT_PADDING;
    const minTop = VIEWPORT_PADDING;

    // If form would go off bottom, position above cursor instead
    if (top > maxTop) {
      return Math.max(minTop, clientY - formHeight - FORM_OFFSET);
    }

    return Math.max(minTop, top);
  }

  /**
   * Apply calculated position to form
   */
  private applyPosition(position: { left: number; top: number }): void {
    if (!this.formElement) return;

    this.formElement.style.left = `${position.left}px`;
    this.formElement.style.top = `${position.top}px`;
  }

  /**
   * Focus and select the text input
   */
  private focusInput(): void {
    if (this.textInput) {
      this.textInput.focus();
      this.textInput.select();
    }
  }

  /**
   * Set up handler to close form when clicking outside
   */
  private setupOutsideClickHandler(): void {
    setTimeout(() => {
      document.addEventListener('click', this.handleOutsideClick);
    }, 0);
  }

  /**
   * Hide the form
   */
  hide(): void {
    if (this.formElement) {
      this.formElement.classList.add('hidden');
    }
    if (this.textInput) {
      this.textInput.value = '';
    }
    this.annotation = null;
    document.removeEventListener('click', this.handleOutsideClick);
  }

  /**
   * Handle clicks outside the form
   */
  private handleOutsideClick = (e: MouseEvent): void => {
    if (this.formElement && !this.formElement.contains(e.target as Node)) {
      this.hide();
    }
  };

  /**
   * Render the form
   */
  private render(): void {
    this.innerHTML = '';

    this.formElement = document.createElement('div');
    this.formElement.className =
      'fixed z-50 bg-white dark:bg-gray-800 rounded-lg shadow-xl border border-gray-200 dark:border-gray-700 p-4 min-w-[300px]';

    // Form title
    const title = document.createElement('h4');
    title.className = 'text-sm font-semibold text-gray-900 dark:text-white mb-3';
    title.textContent = this.mode === 'create' ? 'Add Annotation' : 'Edit Annotation';
    this.formElement.appendChild(title);

    // Text input
    const inputLabel = document.createElement('label');
    inputLabel.className = 'block text-xs text-gray-600 dark:text-gray-400 mb-1';
    inputLabel.textContent = 'Annotation Text';
    this.formElement.appendChild(inputLabel);

    this.textInput = document.createElement('input');
    this.textInput.type = 'text';
    this.textInput.placeholder = 'Enter description...';
    this.textInput.className =
      'w-full px-3 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 text-sm mb-3';

    // Handle enter key to submit
    this.textInput.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        this.handleSubmit();
      } else if (e.key === 'Escape') {
        e.preventDefault();
        this.hide();
      }
    });

    this.formElement.appendChild(this.textInput);

    // Buttons container
    const buttonsContainer = document.createElement('div');
    buttonsContainer.className = 'flex justify-end gap-2';

    // Cancel button
    const cancelButton = document.createElement('button');
    cancelButton.type = 'button';
    cancelButton.className =
      'px-3 py-1.5 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors';
    cancelButton.textContent = 'Cancel';
    cancelButton.addEventListener('click', (e) => {
      e.stopPropagation();
      this.hide();
    });
    buttonsContainer.appendChild(cancelButton);

    // Submit button
    const submitButton = document.createElement('button');
    submitButton.type = 'button';
    submitButton.className =
      'px-3 py-1.5 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors';
    submitButton.textContent = this.mode === 'create' ? 'Add' : 'Update';
    submitButton.addEventListener('click', (e) => {
      e.stopPropagation();
      this.handleSubmit();
    });
    buttonsContainer.appendChild(submitButton);

    // Delete button (only in edit mode)
    if (this.mode === 'edit' && this.annotation?.id) {
      const deleteButton = document.createElement('button');
      deleteButton.type = 'button';
      deleteButton.className =
        'px-3 py-1.5 text-sm bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors';
      deleteButton.textContent = 'Delete';
      deleteButton.addEventListener('click', (e) => {
        e.stopPropagation();
        this.handleDelete();
      });
      buttonsContainer.appendChild(deleteButton);
    }

    this.formElement.appendChild(buttonsContainer);

    this.appendChild(this.formElement);
  }

  /**
   * Handle form submission
   */
  private handleSubmit(): void {
    if (!this.textInput || !this.annotation) return;

    const text = this.textInput.value.trim();
    if (!text) {
      // Show error or just don't submit
      this.textInput.focus();
      return;
    }

    // Update annotation text
    this.annotation.annotationText = text;

    // Emit custom event
    if (this.mode === 'create') {
      this.dispatchEvent(
        new CustomEvent('annotation-create', {
          detail: { annotation: this.annotation },
          bubbles: true,
        })
      );
    } else {
      this.dispatchEvent(
        new CustomEvent('annotation-update', {
          detail: { annotation: this.annotation },
          bubbles: true,
        })
      );
    }

    this.hide();
  }

  /**
   * Handle annotation deletion
   */
  private handleDelete(): void {
    if (!this.annotation?.id) return;

    if (confirm('Are you sure you want to delete this annotation?')) {
      this.dispatchEvent(
        new CustomEvent('annotation-delete', {
          detail: { annotationId: this.annotation.id },
          bubbles: true,
        })
      );
      this.hide();
    }
  }

  /**
   * Clean up when removed from DOM
   */
  disconnectedCallback(): void {
    document.removeEventListener('click', this.handleOutsideClick);
  }
}

// Register the custom element
if (!customElements.get('annotation-form')) {
  customElements.define('annotation-form', AnnotationForm);
}
