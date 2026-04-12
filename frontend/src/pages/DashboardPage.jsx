import { useEffect, useState } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import { getDashboardAnalysis } from '../services/api';
import { 
  Loader2, AlertTriangle, TrendingUp, TrendingDown, 
  Activity, DollarSign, Target, ShoppingBag, Award, AlertCircle,
  ArrowUpRight, ArrowDownRight, Minus, UtensilsCrossed, ChefHat,
  Sparkles, Zap, Info, BarChart3, PieChart as PieIcon, LineChart as LineIcon,
  ThumbsDown, Lightbulb, Star, Flame
} from 'lucide-react';
import { 
  BarChart, Bar, LineChart, Line, PieChart, Pie,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, Cell 
} from 'recharts';

export default function DashboardPage() {
  const { datasetId } = useParams();
  const location = useLocation();
  const isInsightsView = location.pathname.endsWith('/insights');
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let mounted = true;
    const loadData = async () => {
      setLoading(true);
      setError('');
      try {
        const res = await getDashboardAnalysis(datasetId);
        if(mounted) setData(res);
      } catch (err) {
        console.error(err);
        if(mounted) setError('Failed to load dashboard data. Is the backend running?');
      } finally {
        if(mounted) setLoading(false);
      }
    };
    loadData();
    return () => mounted = false;
  }, [datasetId]);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[70vh] text-slate-400">
        <div className="relative">
          <div className="absolute inset-0 bg-orange-500 blur-2xl opacity-20 rounded-full animate-pulse"></div>
          <Loader2 size={56} className="animate-spin relative z-10 text-orange-600 mb-6" />
        </div>
        <p className="font-semibold text-lg tracking-wide text-slate-600">Analyzing Restaurant Data...</p>
        <p className="text-sm text-slate-400 mt-1">Computing item performance, trends & recommendations</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-8 bg-red-50/50 backdrop-blur-sm border border-red-200 rounded-3xl text-red-600 flex flex-col items-center justify-center min-h-[50vh] gap-4">
        <AlertTriangle size={48} className="text-red-500 opacity-80" />
        <h2 className="text-2xl font-bold">Analysis Failed</h2>
        <span className="font-medium">{error}</span>
      </div>
    );
  }

  if (!data) return null;

  const { kpis, topItems, leastSellingItems, monthlyAnalysis, monthOverMonth, trend, menuRecommendations, insights, sections, mostProfitableMonth, profitOptimization } = data;

  // KPI extraction
  const totalRevenue = kpis?.totalRevenue;
  const totalProfit = kpis?.totalProfit;
  const monthlyGrowth = kpis?.monthlyGrowth;
  const totalItemsSold = kpis?.totalItemsSold;
  const topItem = kpis?.topItem;
  const avgOrderValue = kpis?.avgOrderValue;

  // Chart data extraction
  const pieSections = (sections || []).filter(s => (s.chart || s).type === 'pie');
  const lineSections = (sections || []).filter(s => (s.chart || s).type === 'line');
  const barSections = (sections || []).filter(s => (s.chart || s).type === 'bar');

  return (
    <div className="space-y-8 bg-slate-50/50 min-h-screen pb-12 w-full max-w-7xl mx-auto">
      
      {/* ═══ HEADER ═══ */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-end gap-4 border-b border-slate-200/60 pb-6">
        <div>
          <h1 className="text-4xl font-extrabold text-slate-900 tracking-tight flex items-center gap-3">
            <UtensilsCrossed size={36} className="text-orange-500" />
            Sales Intelligence & Menu Optimization
          </h1>
          <p className="text-slate-500 mt-2 font-medium">Professional Restaurant Business Analytics</p>
        </div>
      </div>

      {/* ═══ KPI CARDS ═══ */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <KpiCard 
          title="Total Revenue" 
          value={totalRevenue?.value != null ? `₹${Number(totalRevenue.value).toLocaleString()}` : 'N/A'} 
          description={totalRevenue?.description}
          icon={<DollarSign size={24} className="text-emerald-500" />}
          gradient="from-emerald-50 to-emerald-100/50"
          borderColor="border-emerald-200"
        />
        <KpiCard 
          title="Total Profit" 
          value={totalProfit?.value != null ? `₹${Number(totalProfit.value).toLocaleString()}` : 'N/A'} 
          description={totalProfit?.description}
          icon={<TrendingUp size={24} className="text-blue-500" />}
          gradient="from-blue-50 to-blue-100/50"
          borderColor="border-blue-200"
        />
        <KpiCard 
          title="Monthly Growth" 
          value={monthlyGrowth?.value != null ? `${monthlyGrowth.value}%` : 'N/A'} 
          description={monthlyGrowth?.description}
          icon={monthlyGrowth?.value > 0 ? <ArrowUpRight size={24} className="text-emerald-500" /> : <ArrowDownRight size={24} className="text-rose-500" />}
          valueColor={monthlyGrowth?.value > 0 ? "text-emerald-600" : (monthlyGrowth?.value < 0 ? "text-rose-600" : "text-slate-800")}
          gradient={monthlyGrowth?.value > 0 ? "from-emerald-50 to-emerald-100/50" : "from-rose-50 to-rose-100/50"}
          borderColor={monthlyGrowth?.value > 0 ? "border-emerald-200" : "border-rose-200"}
        />
        <KpiCard 
          title="Top Item" 
          value={topItem?.value || 'N/A'} 
          description={topItem?.description}
          icon={<Award size={24} className="text-amber-500" />}
          gradient="from-amber-50 to-amber-100/50"
          borderColor="border-amber-200"
        />
      </div>

      {/* ═══ SECONDARY KPIs ═══ */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
        <KpiCard 
          title="Most Profitable Month" 
          value={mostProfitableMonth?.month || 'N/A'} 
          description={mostProfitableMonth?.totalRevenue != null ? `₹${Number(mostProfitableMonth.totalRevenue).toLocaleString()} revenue` : ''}
          icon={<Star size={24} className="text-pink-500" />}
          gradient="from-pink-50 to-pink-100/50"
          borderColor="border-pink-200"
        />
        <KpiCard 
          title="Items Sold" 
          value={totalItemsSold?.value != null ? Number(totalItemsSold.value).toLocaleString() : 'N/A'} 
          description={totalItemsSold?.description}
          icon={<ShoppingBag size={24} className="text-violet-500" />}
          gradient="from-violet-50 to-violet-100/50"
          borderColor="border-violet-200"
        />
        <KpiCard 
          title="Avg Revenue Per Item" 
          value={avgOrderValue?.value != null ? `₹${Number(avgOrderValue.value).toLocaleString()}` : 'N/A'} 
          description={avgOrderValue?.description}
          icon={<Activity size={24} className="text-cyan-500" />}
          gradient="from-cyan-50 to-cyan-100/50"
          borderColor="border-cyan-200"
        />
      </div>

      {/* ═══ MONTH-OVER-MONTH COMPARISON BANNER ═══ */}
      {monthOverMonth && monthOverMonth.summary && !monthOverMonth.summary.includes("Not enough") && (
        <div className={`rounded-3xl p-6 border flex items-center gap-4 shadow-sm ${
          monthOverMonth.direction === 'increased' 
            ? 'bg-gradient-to-r from-emerald-50 to-emerald-100/50 border-emerald-200' 
            : monthOverMonth.direction === 'declined'
            ? 'bg-gradient-to-r from-rose-50 to-rose-100/50 border-rose-200'
            : 'bg-gradient-to-r from-slate-50 to-slate-100/50 border-slate-200'
        }`}>
          <div className={`p-4 rounded-2xl ${
            monthOverMonth.direction === 'increased' ? 'bg-emerald-100' : monthOverMonth.direction === 'declined' ? 'bg-rose-100' : 'bg-slate-100'
          }`}>
            {monthOverMonth.direction === 'increased' 
              ? <TrendingUp size={28} className="text-emerald-600" />
              : monthOverMonth.direction === 'declined'
              ? <TrendingDown size={28} className="text-rose-600" />
              : <Minus size={28} className="text-slate-600" />
            }
          </div>
          <div className="flex-1">
            <h3 className={`text-lg font-bold ${
              monthOverMonth.direction === 'increased' ? 'text-emerald-800' : monthOverMonth.direction === 'declined' ? 'text-rose-800' : 'text-slate-800'
            }`}>
              {monthOverMonth.summary}
            </h3>
            <div className="flex gap-6 mt-1">
              <span className="text-sm text-slate-500">
                Revenue: <strong className={monthOverMonth.revenueGrowthPct > 0 ? 'text-emerald-600' : 'text-rose-600'}>
                  {monthOverMonth.revenueGrowthPct > 0 ? '+' : ''}{monthOverMonth.revenueGrowthPct}%
                </strong>
              </span>
              {monthOverMonth.profitGrowthPct !== 0 && (
                <span className="text-sm text-slate-500">
                  Profit: <strong className={monthOverMonth.profitGrowthPct > 0 ? 'text-emerald-600' : 'text-rose-600'}>
                    {monthOverMonth.profitGrowthPct > 0 ? '+' : ''}{monthOverMonth.profitGrowthPct}%
                  </strong>
                </span>
              )}
            </div>
          </div>
        </div>
      )}

      {!isInsightsView && (
        <>
          {/* ═══ ITEM PERFORMANCE: TOP 5 & BOTTOM 5 ═══ */}
          <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
            {/* Top 5 Items */}
            {topItems && topItems.length > 0 && (
              <div className="bg-white rounded-3xl border border-slate-200/60 shadow-sm overflow-hidden">
                <div className="bg-gradient-to-r from-emerald-600 to-emerald-700 px-6 py-4 flex items-center gap-3">
                  <Flame size={20} className="text-white" />
                  <h3 className="text-white font-bold text-lg">Top 5 Best Sellers</h3>
                </div>
                <div className="p-4">
                  {topItems.map((item, i) => (
                    <div key={i} className="flex items-center gap-4 p-4 hover:bg-slate-50 rounded-2xl transition-colors">
                      <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-emerald-100 to-emerald-200 flex items-center justify-center font-extrabold text-emerald-700 text-lg shrink-0">
                        {i + 1}
                      </div>
                      <div className="flex-1 min-w-0">
                        <h4 className="font-bold text-slate-800 truncate">{item.itemName}</h4>
                        <p className="text-xs text-slate-400">{item.quantitySold} units sold · {item.revenueContributionPct}% of revenue</p>
                      </div>
                      <div className="text-right shrink-0">
                        <p className="font-extrabold text-emerald-600">₹{item.revenue?.toLocaleString()}</p>
                        {item.profit !== 0 && (
                          <p className="text-xs text-slate-400">Profit: ₹{item.profit?.toLocaleString()}</p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Bottom 5 Items */}
            {leastSellingItems && leastSellingItems.length > 0 && (
              <div className="bg-white rounded-3xl border border-slate-200/60 shadow-sm overflow-hidden">
                <div className="bg-gradient-to-r from-rose-500 to-rose-600 px-6 py-4 flex items-center gap-3">
                  <ThumbsDown size={20} className="text-white" />
                  <h3 className="text-white font-bold text-lg">Bottom 5 — Needs Attention</h3>
                </div>
                <div className="p-4">
                  {leastSellingItems.map((item, i) => (
                    <div key={i} className="flex items-center gap-4 p-4 hover:bg-rose-50/50 rounded-2xl transition-colors">
                      <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-rose-100 to-rose-200 flex items-center justify-center shrink-0">
                        <AlertCircle size={18} className="text-rose-500" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <h4 className="font-bold text-slate-800 truncate">{item.itemName}</h4>
                        <p className="text-xs text-slate-400">{item.quantitySold} units sold · {item.revenueContributionPct}% of revenue</p>
                      </div>
                      <div className="text-right shrink-0">
                        <p className="font-extrabold text-rose-600">₹{item.revenue?.toLocaleString()}</p>
                        {item.profit !== 0 && (
                          <p className={`text-xs ${item.profit < 0 ? 'text-rose-500 font-bold' : 'text-slate-400'}`}>
                            Profit: {item.profit < 0 ? '-' : ''}₹{Math.abs(item.profit)?.toLocaleString()}
                          </p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* ═══ CHARTS ═══ */}
          <div className="space-y-8">
            
            {/* Pie + Line Row */}
            {(pieSections.length > 0 || lineSections.length > 0) && (
              <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
                {pieSections.length > 0 && <ChartRenderer section={pieSections[0]} />}
                {lineSections.length > 0 && <ChartRenderer section={lineSections[0]} />}
              </div>
            )}

            {/* Bar Charts */}
            {barSections.map((barSection, index) => (
              <div key={`bar-${index}`} className="w-full">
                <ChartRenderer section={barSection} fullWidth={true} />
              </div>
            ))}
          </div>

          {/* ═══ MENU OPTIMIZATION RECOMMENDATIONS ═══ */}
          {menuRecommendations && menuRecommendations.length > 0 && (
            <div className="bg-white rounded-3xl border border-amber-200 shadow-sm overflow-hidden">
              <div className="bg-gradient-to-r from-amber-500 to-orange-500 px-8 py-4 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <ChefHat className="text-white" size={24} />
                  <h3 className="text-white font-bold text-lg">Menu Optimization Recommendations</h3>
                </div>
                <span className="text-white/80 text-xs font-bold uppercase tracking-widest bg-white/15 px-3 py-1 rounded-full">
                  {menuRecommendations.length} items flagged
                </span>
              </div>
              <div className="p-6 space-y-4">
                {menuRecommendations.map((rec, i) => (
                  <div key={i} className={`rounded-2xl p-5 border flex gap-4 items-start ${
                    rec.severity === 'critical' 
                      ? 'bg-rose-50/50 border-rose-200' 
                      : 'bg-amber-50/50 border-amber-200'
                  }`}>
                    <div className={`p-2 rounded-xl shrink-0 ${
                      rec.severity === 'critical' ? 'bg-rose-100' : 'bg-amber-100'
                    }`}>
                      <AlertCircle size={20} className={rec.severity === 'critical' ? 'text-rose-500' : 'text-amber-500'} />
                    </div>
                    <div className="flex-1">
                      <h4 className="font-bold text-slate-800">{rec.itemName}</h4>
                      <p className="text-sm text-slate-500 mt-1">{rec.reason}</p>
                      <p className={`text-sm font-semibold mt-2 ${
                        rec.severity === 'critical' ? 'text-rose-600' : 'text-amber-600'
                      }`}>
                        → {rec.action}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </>
      )}

      {isInsightsView && (
        <>
          {/* ═══ BUSINESS INSIGHTS ═══ */}
          {insights && insights.length > 0 && (
            <div className="bg-white rounded-3xl border border-indigo-100 shadow-sm overflow-hidden">
              <div className="bg-gradient-to-r from-indigo-600 to-violet-600 px-8 py-4 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <Sparkles className="text-white" size={24} />
                  <h3 className="text-white font-bold text-lg">Restaurant Business Insights</h3>
                </div>
                <span className="text-indigo-100 text-xs font-bold uppercase tracking-widest bg-white/10 px-3 py-1 rounded-full">
                  Data-Driven
                </span>
              </div>
              <div className="p-8">
                <div className="bg-slate-50 rounded-2xl p-6 border border-slate-100">
                  <ul className="space-y-4">
                    {insights.map((insight, i) => (
                      <li key={i} className="flex gap-3 text-sm text-slate-700 leading-relaxed items-start">
                        <span className="text-indigo-500 font-bold text-lg shrink-0 mt-[-2px]">•</span>
                        <span className="font-medium">{insight}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            </div>
          )}

          {/* ═══ PROFIT OPTIMIZATION SUGGESTIONS ═══ */}
          {profitOptimization && profitOptimization.length > 0 && (
            <div className="bg-white rounded-3xl border border-indigo-100 shadow-sm overflow-hidden">
              <div className="bg-gradient-to-r from-teal-600 to-emerald-600 px-8 py-4 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <Lightbulb className="text-white" size={24} />
                  <h3 className="text-white font-bold text-lg">Profit Optimization Suggestions</h3>
                </div>
                <span className="text-teal-100 text-xs font-bold uppercase tracking-widest bg-white/10 px-3 py-1 rounded-full">
                  Actionable
                </span>
              </div>
              <div className="p-8">
                <div className="bg-slate-50 rounded-2xl p-6 border border-slate-100">
                  <ul className="space-y-4">
                    {profitOptimization.map((suggestion, i) => (
                      <li key={i} className="flex gap-3 text-sm text-slate-700 leading-relaxed items-start">
                        <span className="text-teal-500 font-bold text-lg shrink-0 mt-[-2px]">•</span>
                        <span className="font-medium">{suggestion}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            </div>
          )}
        </>
      )}



      {/* Empty state */}
      {(!sections || sections.length === 0) && (!topItems || topItems.length === 0) && (
        <div className="bg-white p-12 rounded-3xl border border-slate-200 text-center shadow-sm">
          <UtensilsCrossed size={48} className="mx-auto text-slate-300 mb-4" />
          <h3 className="text-xl font-bold text-slate-800">No Restaurant Data Detected</h3>
          <p className="text-slate-500 mt-2">Upload a dataset with Item Name, Price, Quantity, and Date columns for restaurant analytics.</p>
        </div>
      )}

    </div>
  );
}

// ═══════════════════════════════════════════════════════════════════
//  COMPONENTS
// ═══════════════════════════════════════════════════════════════════

function KpiCard({ title, value, icon, description, valueColor = "text-slate-800", gradient = "from-slate-50 to-slate-100/50", borderColor = "border-slate-200" }) {
  return (
    <div className={`bg-gradient-to-br ${gradient} p-6 rounded-3xl border ${borderColor} shadow-sm hover:shadow-md transition-shadow relative overflow-hidden group flex flex-col h-full`}>
      <div className="absolute top-0 right-0 -mr-4 -mt-4 w-24 h-24 bg-white/30 rounded-full opacity-50 group-hover:scale-110 transition-transform duration-500"></div>
      <div className="flex justify-between items-start relative z-10 mb-4">
        <div>
          <p className="text-sm font-bold tracking-wide text-slate-400 uppercase mb-1">{title}</p>
          <h3 className={`text-3xl font-extrabold tracking-tight ${valueColor}`}>{value}</h3>
        </div>
        <div className="p-3 bg-white/70 rounded-2xl border border-white/50 backdrop-blur-sm">
          {icon}
        </div>
      </div>
      {description && (
        <div className="mt-auto relative z-10 pt-3 border-t border-white/50">
          <p className="text-xs font-medium text-slate-500 leading-relaxed">
            {description}
          </p>
        </div>
      )}
    </div>
  );
}

function ChartRenderer({ section, fullWidth = false }) {
  const chartTitle = section.title || "Restaurant Visualization";
  const { summary, businessImpact } = section;
  const chart = section.chart || section;
  const { type, labels, data, profitData } = chart;
  
  const COLORS = ['#f97316', '#fb923c', '#fdba74', '#059669', '#10b981', '#6366f1', '#8b5cf6', '#ec4899', '#f43f5e', '#eab308', '#14b8a6'];

  let chartData = [];
  if (labels && data) {
    chartData = labels.map((l, i) => ({ 
      name: l, 
      value: data[i],
      profit: profitData ? profitData[i] : undefined
    }));
  }

  const getPieColor = (entry, index) => {
    if (entry.name === 'Other') return '#cbd5e1'; 
    return COLORS[index % COLORS.length];
  };

  const chartIcon = type === 'bar' ? <BarChart3 size={18} className="text-orange-500" /> 
                   : type === 'pie' ? <PieIcon size={18} className="text-violet-500" />
                   : <LineIcon size={18} className="text-emerald-500" />;

  return (
    <div className="bg-white rounded-[2rem] shadow-[0_2px_20px_rgb(0,0,0,0.02)] border border-slate-200/80 hover:border-slate-300 transition-colors flex flex-col overflow-hidden h-full">
      
      <div className={`p-7 flex flex-col ${fullWidth ? 'h-[450px]' : 'h-[380px]'}`}>
        <div className="mb-4 flex items-center gap-2">
          {chartIcon}
          <h3 className="text-xl font-bold text-slate-800">{chartTitle}</h3>
        </div>
        
        <div className="flex-1 w-full min-h-0 relative">
          <ResponsiveContainer width="100%" height="100%">
            {type === 'bar' ? (
              <BarChart data={chartData} margin={{ top: 5, right: 10, left: 20, bottom: 5 }} layout={fullWidth ? "horizontal" : "vertical"}>
                <CartesianGrid strokeDasharray="3 3" vertical={fullWidth ? false : true} horizontal={fullWidth ? true : false} stroke="#f1f5f9" />
                {fullWidth ? (
                  <>
                    <XAxis dataKey="name" tick={{fontSize: 11, fill: '#64748b'}} axisLine={false} tickLine={false} angle={-25} textAnchor="end" height={60} />
                    <YAxis tick={{fontSize: 12, fill: '#64748b'}} axisLine={false} tickLine={false} />
                  </>
                ) : (
                  <>
                    <XAxis type="number" tick={{fontSize: 12, fill: '#64748b'}} axisLine={false} tickLine={false} />
                    <YAxis dataKey="name" type="category" width={100} tick={{fontSize: 12, fill: '#64748b'}} axisLine={false} tickLine={false} />
                  </>
                )}
                <Tooltip cursor={{fill: '#f8fafc'}} contentStyle={{borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)'}} />
                <Bar dataKey="value" name="Revenue" fill="#f97316" radius={[4, 4, 4, 4]}>
                   {chartData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                   ))}
                </Bar>
                {profitData && (
                  <Bar dataKey="profit" name="Profit" fill="#059669" radius={[4, 4, 4, 4]} />
                )}
                {profitData && <Legend />}
              </BarChart>
            ) : type === 'line' ? (
              <LineChart data={chartData} margin={{ top: 5, right: 10, left: 10, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                <XAxis dataKey="name" tick={{fontSize: 12, fill: '#64748b'}} axisLine={false} tickLine={false} />
                <YAxis tick={{fontSize: 12, fill: '#64748b'}} axisLine={false} tickLine={false} />
                <Tooltip contentStyle={{borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)'}} />
                <Line type="monotone" dataKey="value" name="Revenue" stroke="#f97316" strokeWidth={4} dot={{ r: 4, strokeWidth: 2, fill: '#fff' }} activeDot={{ r: 8, fill: '#f97316', stroke: '#fff', strokeWidth: 2 }} />
                {profitData && (
                  <Line type="monotone" dataKey="profit" name="Profit" stroke="#059669" strokeWidth={3} dot={{ r: 3, strokeWidth: 2, fill: '#fff' }} strokeDasharray="5 5" />
                )}
                <Legend />
              </LineChart>
            ) : type === 'pie' ? (
              <PieChart>
                <Tooltip contentStyle={{borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)'}} />
                {chartData.length <= 12 && <Legend iconType="circle" wrapperStyle={{fontSize: '12px', paddingTop: '20px'}} />}
                <Pie data={chartData} cx="50%" cy="50%" innerRadius={70} outerRadius={110} paddingAngle={2} dataKey="value" stroke="none">
                  {chartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={getPieColor(entry, index)} />
                  ))}
                </Pie>
              </PieChart>
            ) : null}
          </ResponsiveContainer>
        </div>
      </div>

      {/* Business Context */}
      {(summary || businessImpact) && (
        <div className="bg-slate-50 border-t border-slate-100 p-6 flex flex-col gap-4 mt-auto">
           {summary && (
             <div className="flex gap-3 items-start">
                <Info size={18} className="text-orange-500 mt-0.5 shrink-0" />
                <div>
                   <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-1">Observation</h4>
                   <p className="text-sm font-medium text-slate-700 leading-relaxed">{summary}</p>
                </div>
             </div>
           )}
           {businessImpact && (
             <div className="flex gap-3 items-start">
                <Zap size={18} className="text-amber-500 mt-0.5 shrink-0" />
                <div>
                   <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-1">Recommendation</h4>
                   <p className="text-sm font-semibold text-slate-800 leading-relaxed">{businessImpact}</p>
                </div>
             </div>
           )}
        </div>
      )}

    </div>
  );
}
