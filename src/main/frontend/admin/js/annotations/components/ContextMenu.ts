/**
 * Context Menu Web Component
 * Menu that appears when clicking on the image to add annotations
 */

export class ContextMenu extends HTMLElement {
  private menuElement: HTMLDivElement | null = null;
  private xCoord: number = 0;
  private yCoord: number = 0;
  private clientX: number = 0;
  private clientY: number = 0;

  constructor() {
    super();
  }

  connectedCallback(): void {
    this.render();
    this.hide();
  }

  /**
   * Show the context menu at specific pixel coordinates
   */
  show(clientX: number, clientY: number, xCoord: number, yCoord: number): void {
    if (!this.menuElement) return;

    this.xCoord = xCoord;
    this.yCoord = yCoord;
    this.clientX = clientX;
    this.clientY = clientY;

    // Position menu near cursor with slight offset
    this.menuElement.style.left = `${clientX + 10}px`;
    this.menuElement.style.top = `${clientY + 10}px`;

    // Show menu
    this.menuElement.classList.remove('hidden');

    // Close menu when clicking outside
    setTimeout(() => {
      document.addEventListener('click', this.handleOutsideClick);
    }, 0);
  }

  /**
   * Hide the context menu
   */
  hide(): void {
    if (this.menuElement) {
      this.menuElement.classList.add('hidden');
    }
    document.removeEventListener('click', this.handleOutsideClick);
  }

  /**
   * Handle clicks outside the menu
   */
  private handleOutsideClick = (e: MouseEvent): void => {
    if (this.menuElement && !this.menuElement.contains(e.target as Node)) {
      this.hide();
    }
  };

  /**
   * Get the coordinates stored when menu was opened
   */
  getCoordinates(): { xCoord: number; yCoord: number } {
    return { xCoord: this.xCoord, yCoord: this.yCoord };
  }

  /**
   * Render the context menu
   */
  private render(): void {
    this.innerHTML = '';

    this.menuElement = document.createElement('div');
    this.menuElement.className =
      'fixed z-50 bg-white dark:bg-gray-800 rounded-lg shadow-xl border border-gray-200 dark:border-gray-700 py-1 min-w-[200px]';

    // Add Annotation option
    const addOption = this.createMenuItem('Add Annotation', () => {
      this.dispatchEvent(
        new CustomEvent('add-annotation', {
          detail: {
            xCoord: this.xCoord,
            yCoord: this.yCoord,
            clientX: this.clientX,
            clientY: this.clientY,
          },
          bubbles: true,
        })
      );
      this.hide();
    });

    this.menuElement.appendChild(addOption);

    // Cancel option
    const cancelOption = this.createMenuItem('Cancel', () => {
      this.hide();
    });
    cancelOption.classList.add('text-gray-500', 'dark:text-gray-400');

    this.menuElement.appendChild(cancelOption);

    this.appendChild(this.menuElement);
  }

  /**
   * Create a menu item
   */
  private createMenuItem(text: string, onClick: () => void): HTMLButtonElement {
    const button = document.createElement('button');
    button.type = 'button';
    button.className =
      'w-full text-left px-4 py-2 hover:bg-gray-100 dark:hover:bg-gray-700 text-sm text-gray-900 dark:text-white transition-colors';
    button.textContent = text;
    button.addEventListener('click', (e) => {
      e.stopPropagation();
      onClick();
    });
    return button;
  }

  /**
   * Clean up when removed from DOM
   */
  disconnectedCallback(): void {
    document.removeEventListener('click', this.handleOutsideClick);
  }
}

// Register the custom element
if (!customElements.get('context-menu')) {
  customElements.define('context-menu', ContextMenu);
}
