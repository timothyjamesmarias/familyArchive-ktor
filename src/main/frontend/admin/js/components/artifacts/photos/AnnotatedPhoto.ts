/**
 * AnnotatedPhoto Controller
 * Manages annotation markers on a single photo
 */

import type { Annotation } from '../../../annotations/types';
import { AnnotationMarker } from './AnnotationMarker';

export class AnnotatedPhoto {
  private element: HTMLElement;
  private fileId: number;
  private annotations: Annotation[];
  private markers: Map<number, AnnotationMarker> = new Map();

  constructor(element: HTMLElement) {
    this.element = element;

    const fileId = element.dataset.fileId;
    const annotationsData = element.dataset.annotations;

    if (!fileId || !annotationsData) {
      console.warn('Missing file-id or annotations data on annotated photo element');
      return;
    }

    this.fileId = parseInt(fileId);

    try {
      this.annotations = JSON.parse(annotationsData);
    } catch (e) {
      console.error('Failed to parse annotations data:', e);
      this.annotations = [];
      return;
    }

    this.renderMarkers();
  }

  private renderMarkers(): void {
    this.markers.forEach((marker) => marker.remove());
    this.markers.clear();

    this.annotations.forEach((annotation) => {
      if (!annotation.id) return;

      const marker = new AnnotationMarker();
      marker.setAnnotation(annotation);

      this.markers.set(annotation.id, marker);
      this.element.appendChild(marker);
    });
  }

  destroy(): void {
    this.markers.forEach((marker) => marker.remove());
    this.markers.clear();
  }
}

export function initializeAnnotatedPhotos(): void {
  const photoElements = document.querySelectorAll<HTMLElement>('[data-annotated-photo]');

  photoElements.forEach((element) => {
    if (element.dataset.annotatedPhotoInitialized) {
      return;
    }

    new AnnotatedPhoto(element);
    element.dataset.annotatedPhotoInitialized = 'true';
  });
}
