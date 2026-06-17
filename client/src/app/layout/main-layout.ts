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
      min-height: 100vh;
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

    @media (max-width: 900px) {
      .app-layout {
        flex-direction: column;
        height: 100dvh;
      }

      .app-inner-layout {
        height: auto;
        min-height: 0;
      }

      .main-content {
        padding: 18px;
      }
    }

    @media (max-width: 480px) {
      .main-content {
        padding: 14px;
      }
    }
  `]
})
export class MainLayoutComponent {}
