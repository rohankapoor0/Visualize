import { NavLink, useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { clsx } from 'clsx';

export default function Sidebar() {
  const { datasetId } = useParams();
  const { userData, logout } = useAuth();
  const navigate = useNavigate();

  const navItems = [
    { name: 'Upload Data', path: '/app/upload', icon: 'cloud_upload', exact: true },
    ...(datasetId ? [
      { name: 'Dashboard Overview', path: `/app/dashboard/${datasetId}`, icon: 'dashboard', end: true },
      { name: 'Insights Report', path: `/app/dashboard/${datasetId}/insights`, icon: 'insights' },
    ] : [])
  ];

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/login');
    } catch (err) {
      console.error("Failed to logout", err);
    }
  };

  return (
    <aside className="w-72 h-screen fixed left-0 top-0 bg-surface border-r border-outline-variant/20 flex flex-col py-8 px-6 z-50 font-body">
      <div className="mb-10">
        <NavLink to="/" className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-lg bg-primary-container flex items-center justify-center text-white shadow-sm">
            <span className="material-symbols-outlined text-xl">insights</span>
          </div>
          <div>
            <h1 className="text-xl font-extrabold tracking-tighter font-headline text-on-surface">Visualize AI</h1>
            <p className="text-xs text-slate-500 font-medium">Restaurant Intelligence</p>
          </div>
        </NavLink>
      </div>
      
      <nav className="flex-1 space-y-2 mt-4">
        {navItems.map((item) => {
          return (
            <NavLink
              key={item.name}
              to={item.path}
              end={item.end}
              className={({ isActive }) => clsx(
                'flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-300 group',
                isActive 
                  ? 'bg-primary/10 text-primary font-bold' 
                  : 'text-slate-500 hover:text-primary hover:bg-primary/5'
              )}
            >
              <span className="material-symbols-outlined text-xl">{item.icon}</span>
              <span className="text-sm font-medium">{item.name}</span>
            </NavLink>
          );
        })}
      </nav>
      
      <div className="mt-8">
        <button 
          onClick={handleLogout}
          className="w-full flex items-center justify-center gap-2 py-3.5 bg-surface-container-lowest border border-outline-variant/30 text-rose-600 rounded-xl font-bold text-sm hover:bg-rose-50 hover:border-rose-200 transition-all shadow-sm"
        >
          <span className="material-symbols-outlined text-lg">logout</span>
          Sign Out
        </button>
      </div>
    </aside>
  );
}
