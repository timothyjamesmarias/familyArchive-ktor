/**
 * Reusable Modal component
 * Follows the same pattern as UppyUploader - initialized via data attributes
 *
 * Usage:
 * <div data-modal="myModal">
 *   <!-- Modal trigger (optional, can also open programmatically) -->
 *   <button data-modal-trigger="myModal">Open Modal</button>
 * </div>
 *
 * <div data-modal-target="myModal" class="hidden">
 *   <!-- Modal content -->
 * </div>
 */

export class Modal {
  private modalId: string;
  private overlay: HTMLElement | null = null;
  private modalElement: HTMLElement | null = null;
  private isOpenState: boolean = false;
  private onCloseCallback: (() => void) | null = null;

  constructor(modalId: string) {
    this.modalId = modalId;
    this.createModalStructure();
    this.attachEventListeners();
  }

  /**
   * Create the modal overlay and structure
   */
  private createModalStructure(): void {
    // Create overlay
    this.overlay = document.createElement('div');
    this.overlay.className =
      'fixed inset-0 bg-black bg-opacity-50 z-50 hidden flex items-center justify-center p-4';
    this.overlay.dataset.modalOverlay = this.modalId;

    // Create modal container
    const modalContainer = document.createElement('div');
    modalContainer.className =
      'bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-7xl w-full max-h-[90vh] overflow-hidden flex flex-col';
    modalContainer.dataset.modalContainer = this.modalId;

    // Create modal header
    const header = document.createElement('div');
    header.className =
      'flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700';
    header.innerHTML = `
      <h2 class="text-xl font-semibold text-gray-900 dark:text-white" data-modal-title="${this.modalId}">
        Modal Title
      </h2>
      <button
        type="button"
        data-modal-close="${this.modalId}"
        class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
        aria-label="Close modal"
      >
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
        </svg>
      </button>
    `;

    // Create modal body
    const body = document.createElement('div');
    body.className = 'flex-1 overflow-y-auto p-4';
    body.dataset.modalBody = this.modalId;

    // Create modal footer (optional)
    const footer = document.createElement('div');
    footer.className =
      'hidden p-4 border-t border-gray-200 dark:border-gray-700 flex justify-end gap-2';
    footer.dataset.modalFooter = this.modalId;

    // Assemble modal
    modalContainer.appendChild(header);
    modalContainer.appendChild(body);
    modalContainer.appendChild(footer);
    this.overlay.appendChild(modalContainer);

    // Add to document
    document.body.appendChild(this.overlay);

    this.modalElement = modalContainer;
  }

  /**
   * Attach event listeners
   */
  private attachEventListeners(): void {
    // Close on overlay click
    this.overlay?.addEventListener('click', (e) => {
      if (e.target === this.overlay) {
        this.close();
      }
    });

    // Close button
    const closeButton = this.overlay?.querySelector(`[data-modal-close="${this.modalId}"]`);
    closeButton?.addEventListener('click', () => this.close());

    // ESC key to close
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && this.isOpenState) {
        this.close();
      }
    });

    // Trigger buttons
    document.querySelectorAll(`[data-modal-trigger="${this.modalId}"]`).forEach((trigger) => {
      trigger.addEventListener('click', () => this.open());
    });
  }

  /**
   * Open the modal
   */
  open(options?: {
    title?: string;
    content?: string | HTMLElement;
    footer?: string | HTMLElement;
  }): void {
    if (!this.overlay) return;

    // Set title if provided
    if (options?.title) {
      const titleEl = this.overlay.querySelector(`[data-modal-title="${this.modalId}"]`);
      if (titleEl) titleEl.textContent = options.title;
    }

    // Set content if provided
    if (options?.content) {
      const bodyEl = this.overlay.querySelector(`[data-modal-body="${this.modalId}"]`);
      if (bodyEl) {
        if (typeof options.content === 'string') {
          bodyEl.innerHTML = options.content;
        } else {
          bodyEl.innerHTML = '';
          bodyEl.appendChild(options.content);
        }
      }
    }

    // Set footer if provided
    if (options?.footer) {
      const footerEl = this.overlay.querySelector(`[data-modal-footer="${this.modalId}"]`);
      if (footerEl) {
        footerEl.classList.remove('hidden');
        if (typeof options.footer === 'string') {
          footerEl.innerHTML = options.footer;
        } else {
          footerEl.innerHTML = '';
          footerEl.appendChild(options.footer);
        }
      }
    }

    // Show modal
    this.overlay.classList.remove('hidden');
    this.isOpenState = true;

    // Prevent body scroll
    document.body.style.overflow = 'hidden';

    // Focus first focusable element
    const firstFocusable = this.modalElement?.querySelector<HTMLElement>(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    firstFocusable?.focus();
  }

  /**
   * Close the modal
   */
  close(): void {
    if (!this.overlay) return;

    // Call onClose callback if set
    if (this.onCloseCallback) {
      this.onCloseCallback();
    }

    this.overlay.classList.add('hidden');
    this.isOpenState = false;

    // Restore body scroll
    document.body.style.overflow = '';
  }

  /**
   * Check if modal is open
   */
  isOpen(): boolean {
    return this.isOpenState;
  }

  /**
   * Set callback to run when modal closes
   */
  onClose(callback: () => void): void {
    this.onCloseCallback = callback;
  }

  /**
   * Get the modal body element for direct manipulation
   */
  getBody(): HTMLElement | null {
    return this.overlay?.querySelector(`[data-modal-body="${this.modalId}"]`) || null;
  }

  /**
   * Get the modal footer element
   */
  getFooter(): HTMLElement | null {
    return this.overlay?.querySelector(`[data-modal-footer="${this.modalId}"]`) || null;
  }

  /**
   * Destroy the modal (remove from DOM)
   */
  destroy(): void {
    this.close();
    this.overlay?.remove();
    this.overlay = null;
    this.modalElement = null;
  }
}

/**
 * Initialize all modals on the page that have data-modal attributes
 */
export function initializeModals(): Map<string, Modal> {
  const modals = new Map<string, Modal>();

  document.querySelectorAll('[data-modal]').forEach((element) => {
    const modalId = (element as HTMLElement).dataset.modal;
    if (modalId) {
      modals.set(modalId, new Modal(modalId));
    }
  });

  return modals;
}
