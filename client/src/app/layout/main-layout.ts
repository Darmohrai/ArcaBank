import { Component } from '@angular/core';
import {RouterOutlet} from "@angular/router";
import {SidebarComponent} from "../components/sidebar-components/sidebar/sidebar.component";
import {HeaderComponent} from "../components/header/header.component";

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, HeaderComponent],
  template: `
    <div class="app-layout">
      <app-sidebar/>

      <div class="app-inner-layout">
        <app-header/>
        <main class="main-content">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `,
  styles: [`
    .app-layout {
      display: flex;
      height: 100vh;
      overflow: hidden;
    }

    .app-inner-layout {
      flex: 1;
      display: flex;
      flex-direction: column;
      min-width: 0;
      height: 100vh;
    }

    .main-content {
      flex: 1;
      overflow-y: auto;
      padding: 24px;
      box-sizing: border-box;
      width: 100%;
    }

    .main-content::-webkit-scrollbar {
      display: none;
    }

    .main-content {
      scrollbar-width: none;
    }
  `]
})
export class MainLayoutComponent {}
