import { NavLink, useParams } from 'react-router-dom';
import { LayoutDashboard, FileUp, Lightbulb, Workflow } from 'lucide-react';
import { clsx } from 'clsx';

export default function Sidebar() {
  const { datasetId } = useParams();

  const navItems = [
    { name: 'Upload', path: '/app/upload', icon: FileUp, exact: true },
    ...(datasetId ? [
      { name: 'Dashboard', path: `/app/dashboard/${datasetId}`, icon: LayoutDashboard },
      { name: 'Insights', path: `/app/dashboard/${datasetId}/insights`, icon: Lightbulb },
    ] : [])
  ];

  return (
    <aside className="w-64 bg-white border-r border-slate-200 h-screen sticky top-0 flex flex-col">
      <div className="p-6">
        <NavLink to="/" className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white font-bold text-xl">I</div>
          <span className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-indigo-600 to-purple-600">Visualize AI</span>
        </NavLink>
      </div>
      
      <nav className="flex-1 px-4 py-2 space-y-1">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.name}
              to={item.path}
              className={({ isActive }) => clsx(
                'flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors',
                isActive 
                  ? 'bg-indigo-50 text-indigo-700' 
                  : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
              )}
            >
              <Icon size={18} />
              {item.name}
            </NavLink>
          );
        })}
      </nav>
      
      <div className="p-4 border-t border-slate-100">
        <div className="flex items-center gap-3 px-3 py-2">
          <div className="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-700 font-medium">RK</div>
          <div>
            <p className="text-sm font-medium text-slate-700">Rohan Kapoor</p>
            <p className="text-xs text-slate-500">Free Plan</p>
          </div>
        </div>
      </div>
    </aside>
  );
}
