import tinymce, { Editor } from 'tinymce';

// Import TinyMCE theme and plugins
import 'tinymce/models/dom';
import 'tinymce/themes/silver';
import 'tinymce/icons/default';

// Import TinyMCE skins (required for styling)
import 'tinymce/skins/ui/oxide/skin.css';
import 'tinymce/skins/content/default/content.css';

// Import plugins
import 'tinymce/plugins/lists';
import 'tinymce/plugins/link';
import 'tinymce/plugins/image';
import 'tinymce/plugins/table';
import 'tinymce/plugins/code';
import 'tinymce/plugins/wordcount';
import 'tinymce/plugins/fullscreen';
import 'tinymce/plugins/searchreplace';
import 'tinymce/plugins/charmap';
import 'tinymce/plugins/anchor';
import 'tinymce/plugins/insertdatetime';
import 'tinymce/plugins/media';
import 'tinymce/plugins/help';
import 'tinymce/plugins/preview';
import 'tinymce/plugins/quickbars';

/**
 * Encapsulated TinyMCE editor component
 *
 * This component initializes TinyMCE on a textarea element.
 * The textarea's value is updated automatically as the user edits.
 *
 * Usage:
 * <textarea data-tinymce-editor
 *           data-height="500"
 *           data-menubar="true"
 *           name="content">
 * </textarea>
 */
export class TinyMCEEditor {
  private editor: Editor | null = null;
  private element: HTMLTextAreaElement;
  private darkModeObserver: MutationObserver | null = null;

  constructor(element: HTMLTextAreaElement) {
    this.element = element;

    // Get configuration from data attributes
    const height = parseInt(element.dataset.height || '500');
    const menubar = element.dataset.menubar === 'true';

    // Detect dark mode
    const isDarkMode = document.documentElement.classList.contains('dark');

    // Initialize TinyMCE (self-hosted npm version uses GPL license)
    tinymce.init({
      target: element,
      license_key: 'gpl',
      height: height,
      menubar: menubar,
      // Disable URL-based loading (we import CSS directly)
      skin: false,
      content_css: false,
      // Add dark class to body if dark mode is active
      body_class: isDarkMode ? 'dark-mode' : '',
      plugins: [
        'lists',
        'link',
        'image',
        'table',
        'code',
        'wordcount',
        'fullscreen',
        'searchreplace',
        'charmap',
        'anchor',
        'insertdatetime',
        'media',
        'help',
        'preview',
        'quickbars',
      ],
      toolbar:
        'undo redo | blocks fontsize | bold italic underline strikethrough | ' +
        'forecolor backcolor | alignleft aligncenter alignright alignjustify | ' +
        'bullist numlist outdent indent | blockquote hr | ' +
        'link image media table | charmap anchor insertdatetime | ' +
        'searchreplace preview code fullscreen | help',
      toolbar_mode: 'sliding',
      // Block formats available in the dropdown
      block_formats:
        'Paragraph=p; Heading 1=h1; Heading 2=h2; Heading 3=h3; Heading 4=h4; Preformatted=pre',
      // Font size options
      fontsize_formats: '8pt 10pt 12pt 14pt 16pt 18pt 24pt 36pt 48pt',
      // Quick toolbars on text selection
      quickbars_selection_toolbar: 'bold italic | quicklink h2 h3 blockquote',
      quickbars_insert_toolbar: false,
      // Enable browser spellcheck
      browser_spellcheck: true,
      // Paste as text by default (prevents messy formatting)
      paste_as_text: false,
      // Keep line breaks
      remove_linebreaks: false,
      // Better defaults
      element_format: 'html',
      content_style: `
        body {
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
          font-size: 16px;
          line-height: 1.6;
          color: #333;
          background-color: #fff;
          padding: 15px;
        }
        body.dark-mode {
          color: #e5e7eb;
          background-color: #1f2937;
        }
        h1, h2, h3, h4, h5, h6 {
          margin-top: 1.5em;
          margin-bottom: 0.5em;
          font-weight: 600;
          line-height: 1.3;
        }
        body.dark-mode h1,
        body.dark-mode h2,
        body.dark-mode h3,
        body.dark-mode h4,
        body.dark-mode h5,
        body.dark-mode h6 {
          color: #f9fafb;
        }
        h1 { font-size: 2.5em; }
        h2 { font-size: 2em; }
        h3 { font-size: 1.5em; }
        h4 { font-size: 1.25em; }
        p { margin-bottom: 1em; }
        blockquote {
          border-left: 4px solid #ccc;
          margin-left: 0;
          padding-left: 1em;
          color: #666;
          font-style: italic;
        }
        body.dark-mode blockquote {
          border-left-color: #4b5563;
          color: #9ca3af;
        }
        pre {
          background: #f5f5f5;
          padding: 1em;
          border-radius: 4px;
          overflow-x: auto;
        }
        body.dark-mode pre {
          background: #374151;
          color: #e5e7eb;
        }
        table {
          border-collapse: collapse;
          width: 100%;
          margin: 1em 0;
        }
        table td, table th {
          border: 1px solid #ddd;
          padding: 8px;
        }
        body.dark-mode table td,
        body.dark-mode table th {
          border-color: #4b5563;
        }
        table th {
          background-color: #f5f5f5;
          font-weight: 600;
        }
        body.dark-mode table th {
          background-color: #374151;
        }
        fa-artifact {
          display: block;
          padding: 1rem;
          margin: 1.5rem 0;
          background: #f0f0f0;
          border: 2px dashed #ccc;
          border-radius: 4px;
          position: relative;
          font-family: monospace;
          font-size: 14px;
        }
        body.dark-mode fa-artifact {
          background: #374151;
          border-color: #4b5563;
        }
        fa-artifact::before {
          content: "📎 Artifact Embed: " attr(type) " #" attr(id);
          font-weight: bold;
          display: block;
          margin-bottom: 0.5rem;
          color: #666;
        }
        body.dark-mode fa-artifact::before {
          color: #9ca3af;
        }
      `,
      // Allow custom elements
      extended_valid_elements: 'fa-artifact[id|type|caption|show-annotations|show-transcription]',
      custom_elements: 'fa-artifact',
      // Preserve custom elements in source
      verify_html: false,
      // Initialize callback
      setup: (editor: Editor) => {
        editor.on('init', () => {
          this.editor = editor;

          // Set up dark mode observer
          this.darkModeObserver = new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
              if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                const isDarkMode = document.documentElement.classList.contains('dark');
                this.updateDarkMode(isDarkMode);
              }
            });
          });

          // Start observing the document element for class changes
          this.darkModeObserver.observe(document.documentElement, {
            attributes: true,
            attributeFilter: ['class'],
          });
        });
      },
    });
  }

  /**
   * Update the editor's dark mode state
   */
  private updateDarkMode(isDarkMode: boolean): void {
    if (this.editor) {
      const body = this.editor.getBody();
      if (body) {
        if (isDarkMode) {
          body.classList.add('dark-mode');
        } else {
          body.classList.remove('dark-mode');
        }
      }
    }
  }

  /**
   * Get the editor instance
   */
  public getEditor(): Editor | null {
    return this.editor;
  }

  /**
   * Destroy the editor instance
   */
  public destroy(): void {
    // Disconnect dark mode observer
    if (this.darkModeObserver) {
      this.darkModeObserver.disconnect();
      this.darkModeObserver = null;
    }

    // Remove editor
    if (this.editor) {
      this.editor.remove();
      this.editor = null;
    }
  }
}

/**
 * Auto-initialize all TinyMCE editors on the page
 */
export function initializeTinyMCEEditors(): void {
  const editorElements = document.querySelectorAll<HTMLTextAreaElement>('[data-tinymce-editor]');

  editorElements.forEach((element) => {
    // Prevent double initialization
    if (element.dataset.tinymceInitialized) {
      return;
    }

    new TinyMCEEditor(element);
    element.dataset.tinymceInitialized = 'true';
  });
}
