/**
 * API client for annotations
 */

import type { Annotation } from './types';
import { getCsrfHeaders } from '../../../js/shared/csrf';

/**
 * Save annotations for an artifact file
 * Replaces all annotations with the provided list
 */
export async function saveAnnotations(
  fileId: number,
  annotations: Annotation[]
): Promise<Annotation[]> {
  const response = await fetch(`/api/artifact-files/${fileId}/annotations`, {
    method: 'PUT',
    headers: getCsrfHeaders({
      'Content-Type': 'application/json',
    }),
    body: JSON.stringify({
      annotations: annotations.map((a) => ({
        id: a.id,
        annotationText: a.annotationText,
        xCoord: a.xCoord,
        yCoord: a.yCoord,
      })),
    }),
  });

  if (!response.ok) {
    const error = await response.text();
    throw new Error(`Failed to save annotations: ${response.status} ${error}`);
  }

  const data = await response.json();
  return data.annotations;
}
