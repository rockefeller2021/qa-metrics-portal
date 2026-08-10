import { Injectable } from '@angular/core';
import Swal, { SweetAlertIcon } from 'sweetalert2';

/**
 * Servicio centralizado de alertas usando SweetAlert2.
 * Reemplaza todos los confirm(), alert() y notificaciones nativas del browser.
 */
@Injectable({ providedIn: 'root' })
export class AlertService {

  // ── Toast notification (esquina inferior derecha) ─────────────
  private toast = Swal.mixin({
    toast: true,
    position: 'bottom-end',
    showConfirmButton: false,
    timer: 3500,
    timerProgressBar: true,
    customClass: {
      popup:  'swal-toast-popup',
      title:  'swal-toast-title',
    },
    didOpen: (toast) => {
      toast.addEventListener('mouseenter', Swal.stopTimer);
      toast.addEventListener('mouseleave', Swal.resumeTimer);
    }
  });

  /** Toast de éxito — operación completada */
  success(title: string, text?: string): void {
    this.toast.fire({ icon: 'success', title, text });
  }

  /** Toast de error */
  error(title: string, text?: string): void {
    this.toast.fire({ icon: 'error', title, text, timer: 5000 });
  }

  /** Toast de advertencia */
  warning(title: string, text?: string): void {
    this.toast.fire({ icon: 'warning', title, text });
  }

  /** Toast informativo */
  info(title: string, text?: string): void {
    this.toast.fire({ icon: 'info', title, text });
  }

  // ── Diálogos de confirmación ──────────────────────────────────

  /**
   * Diálogo de confirmación estándar.
   * @returns Promise<boolean> — true si el usuario confirma
   */
  async confirm(title: string, text?: string, confirmText = 'Sí, continuar'): Promise<boolean> {
    const result = await Swal.fire({
      title,
      text,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: confirmText,
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#6366f1',
      cancelButtonColor: '#475569',
      background: '#0f172a',
      color: '#f8fafc',
      customClass: {
        popup:          'swal-dialog-popup',
        confirmButton:  'swal-confirm-btn',
        cancelButton:   'swal-cancel-btn',
      }
    });
    return result.isConfirmed;
  }

  /**
   * Diálogo de confirmación de PELIGRO (acción destructiva).
   * El usuario debe escribir "CONFIRMAR" para habilitar el botón.
   * @returns Promise<boolean>
   */
  async confirmDanger(title: string, text?: string): Promise<boolean> {
    const result = await Swal.fire({
      title,
      html: `
        <p style="color:#94a3b8;font-size:0.875rem;margin-bottom:1rem">${text || 'Esta acción no se puede deshacer.'}</p>
        <p style="color:#f87171;font-size:0.8rem;margin-bottom:0.5rem">Escribe <strong style="color:#fca5a5">CONFIRMAR</strong> para continuar:</p>
      `,
      icon: 'warning',
      input: 'text',
      inputPlaceholder: 'CONFIRMAR',
      showCancelButton: true,
      confirmButtonText: '🗑️ Eliminar todo',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#ef4444',
      cancelButtonColor: '#475569',
      background: '#0f172a',
      color: '#f8fafc',
      customClass: {
        popup:          'swal-dialog-popup',
        input:          'swal-danger-input',
        confirmButton:  'swal-danger-btn',
        cancelButton:   'swal-cancel-btn',
      },
      preConfirm: (value: string) => {
        if (value !== 'CONFIRMAR') {
          Swal.showValidationMessage('Debes escribir exactamente: <strong>CONFIRMAR</strong>');
          return false;
        }
        return true;
      }
    });
    return result.isConfirmed;
  }

  /**
   * Diálogo de confirmación de eliminación individual (color rojo).
   */
  async confirmDelete(title: string, text?: string): Promise<boolean> {
    const result = await Swal.fire({
      title,
      text: text || 'Esta acción no se puede deshacer.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#ef4444',
      cancelButtonColor: '#475569',
      background: '#0f172a',
      color: '#f8fafc',
      customClass: {
        popup:          'swal-dialog-popup',
        confirmButton:  'swal-danger-btn',
        cancelButton:   'swal-cancel-btn',
      }
    });
    return result.isConfirmed;
  }
}
