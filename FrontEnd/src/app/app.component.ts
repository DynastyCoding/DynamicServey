import { Component, OnInit, OnDestroy, Renderer2, ElementRef, AfterViewInit, Inject, PLATFORM_ID } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/navbar/navbar.component';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavbarComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit, AfterViewInit, OnDestroy {
  title = 'frontend';
  private cursorElement: HTMLElement | null = null;
  private scanBarElement: HTMLElement | null = null;
  private mouseMoveHandler: (() => void) | null = null;
  private mouseEnterHandler: (() => void) | null = null;
  private mouseLeaveHandler: (() => void) | null = null;
  private mouseDownHandler: (() => void) | null = null;
  private mouseUpHandler: (() => void) | null = null;
  private cardListeners: Map<Element, { mouseMove: (e: MouseEvent) => void; mouseLeave: () => void }> = new Map();

  constructor(
    private renderer: Renderer2,
    private el: ElementRef,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      this.initCursor();
      this.initScanBar();
    }
  }

  ngAfterViewInit() {
    if (isPlatformBrowser(this.platformId)) {
      setTimeout(() => {
        this.initCard3DEffect();
        this.initButtonRipple();
        this.initRowClickEffect();
      }, 100);
    }
  }

  // ============================================================
  // 1. 自訂游標
  // ============================================================
  private initCursor() {
    this.cursorElement = this.renderer.createElement('div');
    this.renderer.addClass(this.cursorElement, 'custom-cursor');
    this.renderer.appendChild(document.body, this.cursorElement);

    this.mouseMoveHandler = this.renderer.listen('document', 'mousemove', (e: Event) => {
      const mouseEvent = e as MouseEvent;
      if (this.cursorElement) {
        this.renderer.setStyle(this.cursorElement, 'left', mouseEvent.clientX + 'px');
        this.renderer.setStyle(this.cursorElement, 'top', mouseEvent.clientY + 'px');
      }
    });

    this.mouseEnterHandler = this.renderer.listen('document', 'mouseover', (e: Event) => {
      const target = e.target as HTMLElement;
      if (target.closest && target.closest('button, mat-card, .mat-mdc-row, mat-chip, a, input, textarea')) {
        this.cursorElement?.classList.add('hover');
      }
    });

    this.mouseLeaveHandler = this.renderer.listen('document', 'mouseout', (e: Event) => {
      const target = e.target as HTMLElement;
      if (target.closest && target.closest('button, mat-card, .mat-mdc-row, mat-chip, a, input, textarea')) {
        this.cursorElement?.classList.remove('hover');
      }
    });

    this.mouseDownHandler = this.renderer.listen('document', 'mousedown', () => {
      this.cursorElement?.classList.add('click');
    });

    this.mouseUpHandler = this.renderer.listen('document', 'mouseup', () => {
      this.cursorElement?.classList.remove('click');
    });
  }

  // ============================================================
  // 2. 掃描線
  // ============================================================
  private initScanBar() {
    this.scanBarElement = this.renderer.createElement('div');
    this.renderer.addClass(this.scanBarElement, 'scan-bar');
    this.renderer.appendChild(document.body, this.scanBarElement);
  }

  // ============================================================
  // 3. 卡片 3D 傾斜（修正型別）
  // ============================================================
  private initCard3DEffect() {
    const cards = document.querySelectorAll('mat-card');
    cards.forEach((card) => {
      const htmlCard = card as HTMLElement;

      const mouseMove = (e: MouseEvent) => {
        const rect = htmlCard.getBoundingClientRect();
        const x = (e.clientX - rect.left) / rect.width - 0.5;
        const y = (e.clientY - rect.top) / rect.height - 0.5;
        htmlCard.style.transform = `
          translateY(-8px) scale(1.01)
          rotateX(${y * -4}deg)
          rotateY(${x * 4}deg)
        `;
      };

      const mouseLeave = () => {
        htmlCard.style.transform = '';
      };

      // 使用 addEventListener 搭配 type assertion
      card.addEventListener('mousemove', mouseMove as EventListener);
      card.addEventListener('mouseleave', mouseLeave as EventListener);

      this.cardListeners.set(card, { mouseMove, mouseLeave });
    });
  }

  // ============================================================
  // 4. 按鈕點擊波紋（修正型別）
  // ============================================================
  private initButtonRipple() {
    const buttons = document.querySelectorAll('button.mat-mdc-raised-button');
    buttons.forEach((btn) => {
      const button = btn as HTMLElement;

      const clickHandler = (e: MouseEvent) => {
        const ripple = document.createElement('span');
        ripple.className = 'ripple';
        const rect = button.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        ripple.style.width = ripple.style.height = size + 'px';
        ripple.style.left = (e.clientX - rect.left - size/2) + 'px';
        ripple.style.top = (e.clientY - rect.top - size/2) + 'px';
        button.appendChild(ripple);
        setTimeout(() => ripple.remove(), 600);
      };

      button.addEventListener('click', clickHandler as EventListener);
    });
  }

  // ============================================================
  // 5. 表格點擊縮放（修正型別）
  // ============================================================
  private initRowClickEffect() {
    const rows = document.querySelectorAll('.mat-mdc-row');
    rows.forEach((row) => {
      const rowElement = row as HTMLElement;

      const mouseDownHandler = () => {
        rowElement.style.transform = 'scale(0.98)';
      };

      const mouseUpHandler = () => {
        rowElement.style.transform = '';
      };

      const mouseLeaveHandler = () => {
        rowElement.style.transform = '';
      };

      rowElement.addEventListener('mousedown', mouseDownHandler as EventListener);
      rowElement.addEventListener('mouseup', mouseUpHandler as EventListener);
      rowElement.addEventListener('mouseleave', mouseLeaveHandler as EventListener);
    });
  }

  // ============================================================
  // 清理監聽器
  // ============================================================
  ngOnDestroy() {
    if (this.mouseMoveHandler) this.mouseMoveHandler();
    if (this.mouseEnterHandler) this.mouseEnterHandler();
    if (this.mouseLeaveHandler) this.mouseLeaveHandler();
    if (this.mouseDownHandler) this.mouseDownHandler();
    if (this.mouseUpHandler) this.mouseUpHandler();

    if (this.cursorElement) {
      this.renderer.removeChild(document.body, this.cursorElement);
    }

    if (this.scanBarElement) {
      this.renderer.removeChild(document.body, this.scanBarElement);
    }

    this.cardListeners.forEach((listeners, card) => {
      card.removeEventListener('mousemove', listeners.mouseMove as EventListener);
      card.removeEventListener('mouseleave', listeners.mouseLeave as EventListener);
    });
    this.cardListeners.clear();
  }
}
