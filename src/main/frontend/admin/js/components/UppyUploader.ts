import Uppy from '@uppy/core';
import Dashboard from '@uppy/dashboard';

// Constants
const DEFAULT_MAX_FILE_SIZE = 52428800; // 50MB in bytes
const DEFAULT_MAX_NUMBER_OF_FILES = 10;
const DEFAULT_DASHBOARD_HEIGHT = 400; // pixels
const BYTES_PER_MB = 1024 * 1024;

/**
 * Encapsulated Uppy file uploader component
 *
 * This component uses Uppy as a fancy file picker that populates a hidden file input.
 * The form then submits normally with multipart/form-data encoding.
 *
 * Usage:
 * <div data-uppy-uploader
 *      data-form-id="uploadForm"
 *      data-field-name="file"
 *      data-allowed-types="image/*,application/pdf"
 *      data-max-file-size="52428800">
 * </div>
 */
export class UppyUploader {
  private uppy: Uppy;
  private element: HTMLElement;
  private fileInput: HTMLInputElement | null = null;

  constructor(element: HTMLElement) {
    this.element = element;

    // Get configuration from data attributes
    const fieldName = element.dataset.fieldName || 'file';
    const allowedTypes = element.dataset.allowedTypes?.split(',') || null;
    const maxFileSize = parseInt(element.dataset.maxFileSize || String(DEFAULT_MAX_FILE_SIZE));
    const maxNumberOfFiles = parseInt(
      element.dataset.maxNumberOfFiles || String(DEFAULT_MAX_NUMBER_OF_FILES)
    );

    // Initialize Uppy
    this.uppy = new Uppy({
      restrictions: {
        maxFileSize: maxFileSize,
        maxNumberOfFiles: maxNumberOfFiles,
        allowedFileTypes: allowedTypes,
      },
      autoProceed: false,
    });

    // Add Dashboard plugin
    this.uppy.use(Dashboard, {
      inline: true,
      target: element,
      showProgressDetails: false,
      proudlyDisplayPoweredByUppy: false,
      height: DEFAULT_DASHBOARD_HEIGHT,
      note: this.getNoteText(maxFileSize),
    });

    // Set up form integration
    this.setupFormIntegration(fieldName);

    // Handle file added - transfer to hidden input
    this.uppy.on('file-added', () => {
      this.updateFileInput();
    });

    // Handle file removed
    this.uppy.on('file-removed', () => {
      this.updateFileInput();
    });
  }

  private getNoteText(maxFileSize: number): string {
    const maxSizeMB = Math.round(maxFileSize / BYTES_PER_MB);
    return `Maximum file size: ${maxSizeMB}MB`;
  }

  /**
   * Set up integration with a parent form
   * Creates a hidden file input that will be submitted with the form
   */
  private setupFormIntegration(fieldName: string): void {
    const formId = this.element.dataset.formId;
    if (!formId) return;

    const form = document.getElementById(formId) as HTMLFormElement;
    if (!form) return;

    // Create a hidden file input
    this.fileInput = document.createElement('input');
    this.fileInput.type = 'file';
    this.fileInput.name = fieldName;
    this.fileInput.multiple = true;
    this.fileInput.style.display = 'none';
    this.fileInput.required = true;
    form.appendChild(this.fileInput);
  }

  /**
   * Transfer files from Uppy to the hidden file input
   */
  private updateFileInput(): void {
    if (!this.fileInput) return;

    const files = this.uppy.getFiles();

    if (files.length === 0) {
      // No files - clear the input
      this.fileInput.value = '';
      return;
    }

    // Create a DataTransfer object to set files on the input
    const dataTransfer = new DataTransfer();

    files.forEach((file) => {
      if (file.data instanceof File) {
        dataTransfer.items.add(file.data);
      }
    });

    // Set the files on the hidden input
    this.fileInput.files = dataTransfer.files;
  }

  /**
   * Destroy the uploader instance
   */
  public destroy(): void {
    this.uppy.close();
  }
}

/**
 * Auto-initialize all Uppy uploaders on the page
 */
export function initializeUppyUploaders(): void {
  const uploaderElements = document.querySelectorAll<HTMLElement>('[data-uppy-uploader]');

  uploaderElements.forEach((element) => {
    // Prevent double initialization
    if (element.dataset.uppyInitialized) {
      return;
    }

    new UppyUploader(element);
    element.dataset.uppyInitialized = 'true';
  });
}
