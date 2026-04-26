/**
 * Form confirmation utility
 * Adds confirmation dialogs to forms before submission
 */

export interface FormConfirmationOptions {
  formId: string;
  message: string;
}

/**
 * Attaches a confirmation dialog to a form
 * @param options - Configuration options
 */
export function attachFormConfirmation(options: FormConfirmationOptions): void {
  const form = document.getElementById(options.formId);

  if (!form) {
    console.warn(`Form with id "${options.formId}" not found`);
    return;
  }

  form.addEventListener('submit', function (e: Event) {
    if (!confirm(options.message)) {
      e.preventDefault();
    }
  });
}

/**
 * Batch attach confirmation dialogs to multiple forms
 * @param configurations - Array of form confirmation configurations
 */
export function attachMultipleFormConfirmations(configurations: FormConfirmationOptions[]): void {
  configurations.forEach((config) => attachFormConfirmation(config));
}
