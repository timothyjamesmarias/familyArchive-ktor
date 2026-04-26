/**
 * Annotation Marker Web Component
 * Visual marker displayed on an image at annotation coordinates
 */

import type { Annotation } from '../types';

export class AnnotationMarker extends HTMLElement {
  private annotation: Annotation | null = null;
  private markerElement: HTMLDivElement | null = null;

  constructor() {
    super();
  }

  connectedCallback(): void {
    this.classList.add('absolute', 'inset-0', 'pointer-events-none');
    this.style.display = 'block';
    this.render();
  }

  /**
   * Set the annotation data for this marker
   */
  setAnnotation(annotation: Annotation): void {
    this.annotation = annotation;
    this.render();
  }

  /**
   * Get the annotation data
   */
  getAnnotation(): Annotation | null {
    return this.annotation;
  }

  /**
   * Render the marker
   */
  private render(): void {
    if (!this.annotation) return;

    // Clear existing content
    this.innerHTML = '';

    // Create marker element
    this.markerElement = document.createElement('div');
    this.markerElement.className =
      'absolute w-8 h-8 -ml-4 -mt-4 cursor-pointer transform transition-transform hover:scale-110 pointer-events-auto';

    // Position using CSS (will be set by parent)
    this.markerElement.style.left = `${this.annotation.xCoord * 100}%`;
    this.markerElement.style.top = `${this.annotation.yCoord * 100}%`;

    // Create visual marker (dot with ring)
    const dot = document.createElement('div');
    dot.className =
      'w-full h-full rounded-full bg-blue-600 border-3 border-white shadow-lg flex items-center justify-center';
    dot.innerHTML = `
      <svg class="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/>
      </svg>
    `;

    this.markerElement.appendChild(dot);

    // Create tooltip
    const tooltip = document.createElement('div');
    tooltip.className =
      'absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 px-3 py-2 bg-gray-900 text-white text-sm rounded-lg shadow-lg whitespace-nowrap opacity-0 pointer-events-none transition-opacity z-10';
    tooltip.textContent = this.annotation.annotationText;
    this.markerElement.appendChild(tooltip);

    // Show tooltip on hover
    this.markerElement.addEventListener('mouseenter', () => {
      tooltip.classList.remove('opacity-0');
      tooltip.classList.add('opacity-100');
    });

    this.markerElement.addEventListener('mouseleave', () => {
      tooltip.classList.remove('opacity-100');
      tooltip.classList.add('opacity-0');
    });

    // Click handler (will emit custom event)
    this.markerElement.addEventListener('click', (e) => {
      e.stopPropagation();
      this.dispatchEvent(
        new CustomEvent('marker-click', {
          detail: { annotation: this.annotation },
          bubbles: true,
        })
      );
    });

    this.appendChild(this.markerElement);
  }

  /**
   * Update marker position
   */
  updatePosition(xCoord: number, yCoord: number): void {
    if (this.annotation && this.markerElement) {
      this.annotation.xCoord = xCoord;
      this.annotation.yCoord = yCoord;
      this.markerElement.style.left = `${xCoord * 100}%`;
      this.markerElement.style.top = `${yCoord * 100}%`;
    }
  }

  /**
   * Highlight this marker
   */
  highlight(): void {
    if (this.markerElement) {
      this.markerElement.classList.add('ring-4', 'ring-yellow-400');
    }
  }

  /**
   * Remove highlight from this marker
   */
  unhighlight(): void {
    if (this.markerElement) {
      this.markerElement.classList.remove('ring-4', 'ring-yellow-400');
    }
  }

  /**
   * Remove this marker from the DOM
   */
  destroy(): void {
    this.remove();
  }
}

// Register the custom element
if (!customElements.get('annotation-marker')) {
  customElements.define('annotation-marker', AnnotationMarker);
}
