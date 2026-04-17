import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import Navbar from './Navbar';

export default function Layout() {
  return (
    <div className="bg-surface text-on-surface min-h-screen font-body selection:bg-primary-fixed selection:text-primary">
      <Sidebar />
      <div className="ml-72 flex flex-col min-h-screen">
        <Navbar />
        <main className="flex-1">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
