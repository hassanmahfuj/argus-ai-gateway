import { Component, ElementRef, inject, HostListener } from '@angular/core';
import { CommonModule, NgOptimizedImage } from '@angular/common';
import { RouterLink } from '@angular/router';


@Component({
  selector: 'app-header',
  imports: [CommonModule, NgOptimizedImage, RouterLink],
  templateUrl: './header.component.html'
})
export class HeaderComponent {

  elRef = inject(ElementRef);

  @HostListener('document:click', ['$event'])
  handleDropdown(event: Event) {
    // close any open dropdown
    const $clickedDropdown = (event.target as HTMLElement).closest('.js-dropdown');
    const $dropdowns = this.elRef.nativeElement.querySelectorAll('.js-dropdown');
    $dropdowns.forEach(($dropdown:HTMLElement) => {
      if ($clickedDropdown !== $dropdown && $dropdown.getAttribute('data-dropdown-keepopen') !== 'true') {
        $dropdown.ariaExpanded = 'false';
        $dropdown.nextElementSibling!.classList.add('hidden');
      }
    });
    // toggle selected if applicable
    if ($clickedDropdown) {
      $clickedDropdown.ariaExpanded = '' + ($clickedDropdown.ariaExpanded !== 'true');
      $clickedDropdown.nextElementSibling!.classList.toggle('hidden');
    }
  }

}
