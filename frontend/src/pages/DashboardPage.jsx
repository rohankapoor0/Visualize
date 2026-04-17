import { useEffect, useState } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import { getDashboardAnalysis } from '../services/api';
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
      <div className="flex flex-col items-center justify-center min-h-[70vh] text-slate-400 font-body">
        <div className="relative">
          <div className="absolute inset-0 bg-primary blur-2xl opacity-20 rounded-full animate-pulse"></div>
          <span className="material-symbols-outlined text-transparent bg-clip-text bg-gradient-to-r from-primary to-purple-600 animate-spin relative z-10 text-6xl mb-6 font-bold" style={{display: 'inline-block'}}>refresh</span>
        </div>
        <p className="font-extrabold font-headline text-lg tracking-wide text-slate-800">Deep Learning Analysis in Progress</p>
        <p className="text-sm font-medium text-slate-500 mt-2">Computing non-obvious correlations in your sales data...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-8 bg-error-container/50 border border-error/20 rounded-3xl text-error flex flex-col items-center justify-center min-h-[50vh] gap-4 font-body shadow-sm">
        <span className="material-symbols-outlined text-5xl opacity-80">warning</span>
        <h2 className="text-2xl font-extrabold font-headline text-on-error-container">Analysis Interrupted</h2>
        <span className="font-medium text-sm text-on-error-container/80">{error}</span>
      </div>
    );
  }

  if (!data) return null;

  const { kpis, topItems, leastSellingItems, monthlyAnalysis, monthOverMonth, trend, menuRecommendations, insights, sections, mostProfitableMonth, profitOptimization } = data;

  const totalRevenue = kpis?.totalRevenue;
  const totalProfit = kpis?.totalProfit;
  const monthlyGrowth = kpis?.monthlyGrowth;
  const totalItemsSold = kpis?.totalItemsSold;
  const topItem = kpis?.topItem;
  const avgOrderValue = kpis?.avgOrderValue;

  const pieSections = (sections || []).filter(s => (s.chart || s).type === 'pie');
  const lineSections = (sections || []).filter(s => (s.chart || s).type === 'line');
  const barSections = (sections || []).filter(s => (s.chart || s).type === 'bar');

  return (
    <div className="space-y-12 bg-surface min-h-screen pb-12 w-full max-w-7xl mx-auto font-body p-8 lg:p-12">
      
      {/* ═══ HEADER ═══ */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-end gap-4 border-b border-outline-variant/20 pb-8">
        <div>
          <h1 className="text-4xl lg:text-5xl font-extrabold font-headline text-on-surface tracking-tighter flex items-center gap-4">
            <span className="p-3 bg-primary-container rounded-xl text-white shadow-sm flex items-center justify-center">
              <span className="material-symbols-outlined text-3xl">insights</span>
            </span>
            {isInsightsView ? "Insights Report" : "Sales Intelligence"}
          </h1>
          <p className="text-on-surface-variant mt-3 text-lg">
            {isInsightsView ? "Data-driven strategic recommendations." : "We don't just show you numbers; we tell you what they mean."}
          </p>
        </div>
      </div>

      {!isInsightsView && (
        <>

      {/* ═══ HERO KPI BENTO ═══ */}
      <section className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {/* Primary Stat */}
        <div className="md:col-span-2 bg-surface-container-lowest p-8 rounded-[2rem] flex flex-col justify-between shadow-sm border border-outline-variant/10 relative overflow-hidden group">
          <div className="absolute top-0 right-0 w-48 h-48 bg-primary/5 rounded-bl-full group-hover:scale-110 transition-transform duration-700"></div>
          <div className="relative z-10">
            <p className="text-on-surface-variant font-bold text-sm uppercase tracking-widest mb-2">Total Revenue</p>
            <h2 className="text-5xl lg:text-6xl font-extrabold font-headline tracking-tighter text-on-surface drop-shadow-sm">
              {totalRevenue?.value != null ? `₹${Number(totalRevenue.value).toLocaleString()}` : 'N/A'}
            </h2>
          </div>
          <div className="mt-12 flex items-center gap-2 text-primary font-bold relative z-10 bg-primary/5 w-fit px-4 py-2 rounded-full border border-primary/10">
            <span className="material-symbols-outlined text-sm">schedule</span>
            <span className="text-sm">{totalRevenue?.description || 'All time'}</span>
          </div>
        </div>

        {/* Secondary Stat */}
        <div className="bg-surface-container-lowest p-8 rounded-[2rem] shadow-sm border border-outline-variant/10 flex flex-col justify-between group">
          <div className="w-12 h-12 bg-tertiary-fixed rounded-xl flex items-center justify-center text-on-tertiary-fixed-variant mb-6 group-hover:scale-110 transition-transform shadow-inner">
            <span className="material-symbols-outlined text-2xl">account_balance_wallet</span>
          </div>
          <div>
            <p className="text-on-surface-variant font-bold text-xs uppercase tracking-widest mb-2">Total Profit</p>
            <h3 className="text-4xl font-extrabold font-headline tracking-tighter text-on-surface">
              {totalProfit?.value != null ? `₹${Number(totalProfit.value).toLocaleString()}` : 'N/A'}
            </h3>
            <p className="text-xs text-on-surface-variant mt-3 font-medium border-t border-outline-variant/20 pt-3">{totalProfit?.description || 'After calculated item costs'}</p>
          </div>
        </div>

        {/* Growth Stat */}
        <div className="bg-surface-container-lowest p-8 rounded-[2rem] shadow-sm border border-outline-variant/10 flex flex-col justify-between group">
          <div className={`w-12 h-12 rounded-xl flex items-center justify-center mb-6 shadow-inner group-hover:scale-110 transition-transform ${
            monthlyGrowth?.value > 0 ? 'bg-primary/10 text-primary' : 'bg-error-container text-error'
          }`}>
            <span className="material-symbols-outlined text-2xl">
              {monthlyGrowth?.value > 0 ? 'show_chart' : 'trending_down'}
            </span>
          </div>
          <div>
            <p className="text-on-surface-variant font-bold text-xs uppercase tracking-widest mb-2">Monthly Growth</p>
            <h3 className={`text-4xl font-extrabold font-headline tracking-tighter ${
              monthlyGrowth?.value > 0 ? 'text-primary' : 'text-error'
            }`}>
              {monthlyGrowth?.value != null ? `${monthlyGrowth.value > 0 ? '+' : ''}${monthlyGrowth.value}%` : 'N/A'}
            </h3>
            <p className={`text-xs mt-3 font-bold border-t border-outline-variant/20 pt-3 ${
              monthlyGrowth?.value > 0 ? 'text-primary/70' : 'text-error/70'
            }`}>
              {monthlyGrowth?.description || 'vs previous month'}
            </p>
          </div>
        </div>

        {/* Highlight Item */}
        <div className="md:col-span-2 signature-gradient text-white p-8 rounded-[2rem] flex flex-col md:flex-row items-center justify-between shadow-xl shadow-indigo-500/20 relative overflow-hidden">
          <div className="relative z-10 flex-1 pr-6">
            <span className="px-3 py-1 bg-white/20 backdrop-blur-md rounded-full text-[10px] font-extrabold uppercase tracking-widest mb-4 inline-block border border-white/20">Volume Leader</span>
            <h2 className="text-4xl font-extrabold font-headline mb-3 text-white drop-shadow-md">
              {topItem?.value || 'N/A'}
            </h2>
            <p className="text-white/80 max-w-xs text-sm font-medium leading-relaxed">
              {topItem?.description || 'Highest contributing item based on unit sales volume and consistency.'}
            </p>
          </div>
          <div className="relative w-40 h-40 mt-6 md:mt-0 opacity-20">
            <span className="material-symbols-outlined absolute inset-0 flex items-center justify-center text-[150px]">lunch_dining</span>
          </div>
        </div>

        {/* Most Profitable Month */}
        <div className="md:col-span-2 bg-gradient-to-r from-tertiary-container to-tertiary p-8 rounded-[2rem] flex flex-row items-center justify-between shadow-xl shadow-orange-500/10 relative overflow-hidden text-white group">
          <div className="relative z-10">
            <span className="px-3 py-1 bg-white/20 backdrop-blur-md rounded-full text-[10px] font-extrabold uppercase tracking-widest mb-4 inline-block border border-white/20">Peak Performance</span>
            <h2 className="text-4xl lg:text-5xl font-extrabold font-headline mb-3 text-white drop-shadow-md">
              {mostProfitableMonth?.month || 'N/A'}
            </h2>
            <p className="text-white/80 text-sm font-medium">
              {mostProfitableMonth?.totalRevenue != null ? `₹${Number(mostProfitableMonth.totalRevenue).toLocaleString()} revenue during this period` : 'Revenue tracking not available'}
            </p>
          </div>
          <div className="w-20 h-20 bg-white/20 rounded-2xl flex items-center justify-center backdrop-blur-md shadow-inner group-hover:scale-110 transition-transform">
            <span className="material-symbols-outlined text-5xl">hotel_class</span>
          </div>
        </div>

        {/* Other Minor KPIs */}
        <div className="md:col-span-2 bg-surface-container-low p-6 rounded-[2rem] border border-outline-variant/10 flex items-center justify-between gap-4 group">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-white rounded-xl flex items-center justify-center shadow-sm text-secondary group-hover:scale-110 transition-transform">
              <span className="material-symbols-outlined">inventory_2</span>
            </div>
            <div>
              <p className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1">Items Sold</p>
              <p className="text-2xl font-extrabold font-headline text-on-surface">
                {totalItemsSold?.value != null ? Number(totalItemsSold.value).toLocaleString() : 'N/A'}
              </p>
            </div>
          </div>
          <p className="text-xs text-slate-400 font-medium max-w-[120px] text-right">{totalItemsSold?.description}</p>
        </div>
        
        <div className="md:col-span-2 bg-surface-container-low p-6 rounded-[2rem] border border-outline-variant/10 flex items-center justify-between gap-4 group">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-white rounded-xl flex items-center justify-center shadow-sm text-secondary group-hover:scale-110 transition-transform">
              <span className="material-symbols-outlined">receipt_long</span>
            </div>
            <div>
              <p className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1">Avg Vol/Item</p>
              <p className="text-2xl font-extrabold font-headline text-on-surface">
                {avgOrderValue?.value != null ? `₹${Number(avgOrderValue.value).toLocaleString()}` : 'N/A'}
              </p>
            </div>
          </div>
          <p className="text-xs text-slate-400 font-medium max-w-[120px] text-right">{avgOrderValue?.description}</p>
        </div>

      </section>

      {/* ═══ MONTH-OVER-MONTH COMPARISON BANNER ═══ */}
      {monthOverMonth && monthOverMonth.summary && !monthOverMonth.summary.includes("Not enough") && (
        <div className={`rounded-[2rem] p-8 border flex items-center gap-6 shadow-sm ${
          monthOverMonth.direction === 'increased' 
            ? 'bg-gradient-to-r from-surface-container-lowest to-surface-container-lowest border-primary/20' 
            : monthOverMonth.direction === 'declined'
            ? 'bg-gradient-to-r from-surface-container-lowest to-error-container/20 border-error/20'
            : 'bg-surface-container-lowest border-outline-variant/20'
        }`}>
          <div className={`w-16 h-16 rounded-2xl flex items-center justify-center shrink-0 shadow-inner ${
            monthOverMonth.direction === 'increased' ? 'bg-primary/10' 
            : monthOverMonth.direction === 'declined' ? 'bg-error/10' 
            : 'bg-surface-container'
          }`}>
            <span className={`material-symbols-outlined text-3xl font-bold ${
              monthOverMonth.direction === 'increased' ? 'text-primary' 
              : monthOverMonth.direction === 'declined' ? 'text-error' : 'text-on-surface-variant'
            }`}>
              {monthOverMonth.direction === 'increased' ? 'trending_up' : monthOverMonth.direction === 'declined' ? 'trending_down' : 'horizontal_rule'}
            </span>
          </div>
          <div className="flex-1">
            <p className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Month over Month Intelligence</p>
            <h3 className="text-xl lg:text-2xl font-extrabold font-headline text-on-surface tracking-tight">
              {monthOverMonth.summary}
            </h3>
            <div className="flex gap-8 mt-4 border-t border-outline-variant/10 pt-4">
              <span className="text-sm font-medium text-on-surface-variant">
                Revenue Growth: <strong className={`ml-1 ${monthOverMonth.revenueGrowthPct > 0 ? 'text-primary' : 'text-error'}`}>
                  {monthOverMonth.revenueGrowthPct > 0 ? '+' : ''}{monthOverMonth.revenueGrowthPct}%
                </strong>
              </span>
              {monthOverMonth.profitGrowthPct !== 0 && (
                <span className="text-sm font-medium text-on-surface-variant">
                  Profit Growth: <strong className={`ml-1 ${monthOverMonth.profitGrowthPct > 0 ? 'text-primary' : 'text-error'}`}>
                    {monthOverMonth.profitGrowthPct > 0 ? '+' : ''}{monthOverMonth.profitGrowthPct}%
                  </strong>
                </span>
              )}
            </div>
          </div>
        </div>
      )}

      {/* ═══ INSIGHTS VIEW ONLY ═══ */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
            
            {/* Top Sellers */}
            {topItems && topItems.length > 0 && (
              <div className="space-y-6">
                <div className="flex items-end justify-between px-2">
                  <div>
                    <h2 className="text-3xl font-extrabold font-headline text-on-surface tracking-tighter">Top Performers</h2>
                    <p className="text-slate-500 text-sm mt-1 font-medium">Volume leaders & high-margin stars</p>
                  </div>
                  <span className="text-primary font-bold text-sm cursor-pointer hover:underline">View All</span>
                </div>
                <div className="bg-surface-container-lowest rounded-[2rem] overflow-hidden shadow-sm border border-outline-variant/20 p-2">
                  <div className="divide-y divide-slate-100">
                    {topItems.map((item, i) => (
                      <div key={i} className="p-4 flex items-center justify-between hover:bg-surface transition-colors rounded-2xl group">
                        <div className="flex items-center gap-5">
                          <div className="w-12 h-12 bg-surface-container rounded-xl flex items-center justify-center font-extrabold text-slate-400 group-hover:bg-primary/10 group-hover:text-primary transition-colors">
                            {String(i + 1).padStart(2, '0')}
                          </div>
                          <div>
                            <p className="font-extrabold text-on-surface text-lg leading-tight">{item.itemName}</p>
                            <p className="text-xs font-medium text-slate-500 mt-1">{item.quantitySold} units sold</p>
                          </div>
                        </div>
                        <div className="text-right">
                          <p className="font-extrabold text-on-surface text-lg">₹{item.revenue?.toLocaleString()}</p>
                          <div className="flex items-center justify-end gap-1 text-primary text-[10px] font-bold mt-1 bg-primary/5 px-2 py-0.5 rounded-full w-fit ml-auto">
                            <span className="material-symbols-outlined text-[14px]">arrow_upward</span>
                            {item.revenueContributionPct}% share
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {/* Bottom Sellers */}
            {leastSellingItems && leastSellingItems.length > 0 && (
              <div className="space-y-6">
                <div className="flex items-end justify-between px-2">
                  <div>
                    <h2 className="text-3xl font-extrabold font-headline text-on-surface tracking-tighter">Needs Attention</h2>
                    <p className="text-slate-500 text-sm mt-1 font-medium">Underperforming items & wastage risk</p>
                  </div>
                  <div className="flex items-center gap-1.5 text-error font-extrabold text-[10px] uppercase tracking-widest bg-error/10 px-3 py-1.5 rounded-full border border-error/20">
                    <span className="material-symbols-outlined text-sm">warning</span> Action Required
                  </div>
                </div>
                <div className="bg-surface-container-low rounded-[2rem] overflow-hidden shadow-inner border border-outline-variant/20 p-6">
                  <table className="w-full text-left border-separate border-spacing-y-2">
                    <thead>
                      <tr className="text-slate-400 text-xs font-bold uppercase tracking-widest">
                        <th className="px-4 py-3 font-bold">Item Name</th>
                        <th className="px-4 py-3 font-bold text-right">Revenue</th>
                      </tr>
                    </thead>
                    <tbody>
                      {leastSellingItems.map((item, i) => (
                        <tr key={i} className="bg-surface-container-lowest rounded-xl group hover:shadow-md transition-shadow">
                          <td className="px-4 py-4 rounded-l-xl border-y border-l border-outline-variant/10 group-hover:border-slate-300">
                            <div>
                              <p className="font-extrabold text-on-surface">{item.itemName}</p>
                              <div className="flex items-center gap-2 mt-1">
                                <span className="text-[10px] bg-error/10 text-error px-2 py-0.5 rounded-md font-bold uppercase tracking-wider border border-error/5 flex items-center gap-1 w-fit">
                                  <span className="material-symbols-outlined text-[12px]">trending_down</span> {item.quantitySold || 0} Units Sold
                                </span>
                                {item.profit < 0 && (
                                  <span className="text-[10px] bg-slate-100 text-slate-500 px-2 py-0.5 rounded-md font-bold uppercase tracking-wider border border-slate-200">
                                    Negative Margin
                                  </span>
                                )}
                              </div>
                            </div>
                          </td>
                          <td className="px-4 py-4 rounded-r-xl text-right border-y border-r border-outline-variant/10 group-hover:border-slate-300">
                            <p className="font-extrabold text-slate-500">₹{item.revenue?.toLocaleString()}</p>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
            
          </div>

          {/* ═══ CHARTS ═══ */}
          <div className="space-y-12 pt-12">
            
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
        </>
      )}

      {/* ═══ INSIGHTS VIEW ONLY ═══ */}
      {isInsightsView && (
        <div className="grid grid-cols-1 gap-12 mt-8">
          {/* Executive Insights */}
          {insights && insights.length > 0 && (
            <div className="bg-surface-container-lowest rounded-[2.5rem] border border-outline-variant/20 shadow-sm overflow-hidden flex flex-col md:flex-row">
              <div className="bg-surface p-10 md:w-1/3 border-b md:border-b-0 md:border-r border-outline-variant/20 flex flex-col justify-center relative overflow-hidden">
                <div className="absolute top-10 right-10 text-primary opacity-10">
                  <span className="material-symbols-outlined text-[120px]">auto_awesome</span>
                </div>
                <div className="w-16 h-16 bg-primary/10 rounded-2xl flex items-center justify-center text-primary mb-8 border border-primary/10 shadow-inner relative z-10">
                  <span className="material-symbols-outlined text-3xl">psychology</span>
                </div>
                <h3 className="text-3xl font-extrabold font-headline text-on-surface mb-3 relative z-10 tracking-tight">Executive AI Insights</h3>
                <p className="text-on-surface-variant font-medium leading-relaxed relative z-10 text-sm">
                  Strategic observations generated from multidimensional data correlation.
                </p>
              </div>
              <div className="p-10 md:w-2/3">
                <ul className="space-y-6 relative">
                  {insights.map((insight, i) => (
                    <li key={i} className="flex gap-5 text-on-surface leading-relaxed items-start group">
                      <span className="w-8 h-8 rounded-full bg-surface border border-outline-variant/30 flex items-center justify-center text-primary font-bold text-sm shrink-0 group-hover:bg-primary group-hover:text-white transition-colors mt-0.5">
                        {i + 1}
                      </span>
                      <span className="font-medium text-[15px] pt-1">{insight}</span>
                    </li>
                  ))}
                  <div className="absolute left-4 top-8 bottom-4 w-px bg-outline-variant/20 -z-10 hidden sm:block"></div>
                </ul>
              </div>
            </div>
          )}


        </div>
      )}

      {/* Empty state */}
      {(!sections || sections.length === 0) && (!topItems || topItems.length === 0) && (
        <div className="bg-surface-container-lowest p-16 rounded-[3rem] border border-outline-variant/20 text-center shadow-sm max-w-2xl mx-auto flex flex-col items-center justify-center mt-12 relative overflow-hidden">
          <div className="w-24 h-24 bg-surface rounded-full flex items-center justify-center mb-6 shadow-inner border border-outline-variant/10">
            <span className="material-symbols-outlined text-4xl text-slate-300">hourglass_empty</span>
          </div>
          <h3 className="text-2xl font-extrabold font-headline text-slate-800 mb-2">Insufficient Processing Data</h3>
          <p className="text-slate-500 font-medium">Upload a valid dataset containing Item names, Revenue/Price parameters, and Date columns for complete synthesis.</p>
        </div>
      )}

    </div>
  );
}

// ═══════════════════════════════════════════════════════════════════
//  COMPONENTS
// ═══════════════════════════════════════════════════════════════════

function ChartRenderer({ section, fullWidth = false }) {
  const chartTitle = section.title || "Restaurant Visualization";
  const { summary, businessImpact } = section;
  const chart = section.chart || section;
  const { type, labels, data, profitData } = chart;
  
  const COLORS = ['#3525cd', '#4f46e5', '#a44100', '#58579b', '#7b2f00', '#c3c0ff', '#140f54', '#ffb695'];

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

  const chartIcon = type === 'bar' ? 'bar_chart' : type === 'pie' ? 'pie_chart' : 'show_chart';

  return (
    <div className="bg-surface-container-lowest rounded-[2.5rem] shadow-sm border border-outline-variant/20 hover:border-primary/20 transition-colors flex flex-col overflow-hidden h-full group">
      
      <div className={`p-8 lg:p-10 flex flex-col ${fullWidth ? 'h-[550px]' : 'h-[480px]'}`}>
        <div className="mb-8 flex flex-col gap-1">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-surface border border-outline-variant/20 flex items-center justify-center text-primary shadow-sm">
                <span className="material-symbols-outlined text-[18px]">{chartIcon}</span>
            </div>
            <h3 className="text-2xl font-extrabold font-headline text-on-surface tracking-tight">{chartTitle}</h3>
          </div>
        </div>
        
        <div className="flex-1 w-full min-h-0 relative">
          <ResponsiveContainer width="100%" height="100%">
            {type === 'bar' ? (
              <BarChart data={chartData} margin={{ top: 10, right: 10, left: 20, bottom: 10 }} layout={fullWidth ? "horizontal" : "vertical"}>
                <CartesianGrid strokeDasharray="3 3" vertical={fullWidth ? false : true} horizontal={fullWidth ? true : false} stroke="#eaf1ff" />
                {fullWidth ? (
                  <>
                    <XAxis dataKey="name" tick={{fontSize: 12, fill: '#777587', fontWeight: 500}} axisLine={false} tickLine={false} angle={-25} textAnchor="end" height={60} />
                    <YAxis tick={{fontSize: 12, fill: '#777587', fontWeight: 600}} axisLine={false} tickLine={false} />
                  </>
                ) : (
                  <>
                    <XAxis type="number" tick={{fontSize: 12, fill: '#777587', fontWeight: 600}} axisLine={false} tickLine={false} />
                    <YAxis dataKey="name" type="category" width={120} tick={{fontSize: 12, fill: '#777587', fontWeight: 500}} axisLine={false} tickLine={false} />
                  </>
                )}
                <Tooltip cursor={{fill: '#f8f9ff'}} contentStyle={{borderRadius: '16px', border: '1px solid #c7c4d8', boxShadow: '0 20px 40px -10px rgba(11, 28, 48, 0.1)', fontFamily: 'Inter', fontWeight: 600}} />
                <Bar dataKey="value" name="Revenue" fill="#3525cd" radius={[6, 6, 6, 6]}>
                   {chartData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                   ))}
                </Bar>
                {profitData && (
                  <Bar dataKey="profit" name="Profit" fill="#a44100" radius={[6, 6, 6, 6]} />
                )}
                {profitData && <Legend wrapperStyle={{paddingTop: '20px', fontSize: '13px', fontWeight: 600}} />}
              </BarChart>
            ) : type === 'line' ? (
              <LineChart data={chartData} margin={{ top: 10, right: 10, left: 10, bottom: 10 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#eaf1ff" />
                <XAxis dataKey="name" tick={{fontSize: 12, fill: '#777587', fontWeight: 500}} axisLine={false} tickLine={false} />
                <YAxis tick={{fontSize: 12, fill: '#777587', fontWeight: 600}} axisLine={false} tickLine={false} />
                <Tooltip contentStyle={{borderRadius: '16px', border: 'none', boxShadow: '0 20px 40px -10px rgba(11, 28, 48, 0.1)', fontFamily: 'Inter', fontWeight: 600}} />
                <Line type="monotone" dataKey="value" name="Revenue" stroke="#3525cd" strokeWidth={5} dot={{ r: 5, strokeWidth: 3, fill: '#fff' }} activeDot={{ r: 9, fill: '#3525cd', stroke: '#fff', strokeWidth: 3 }} />
                {profitData && (
                  <Line type="monotone" dataKey="profit" name="Profit" stroke="#a44100" strokeWidth={4} dot={{ r: 4, strokeWidth: 2, fill: '#fff' }} strokeDasharray="6 6" />
                )}
                <Legend wrapperStyle={{paddingTop: '20px', fontSize: '13px', fontWeight: 600}} />
              </LineChart>
            ) : type === 'pie' ? (
              <PieChart>
                <Tooltip contentStyle={{borderRadius: '16px', border: '1px solid #c7c4d8', boxShadow: '0 20px 40px -10px rgba(11, 28, 48, 0.1)', fontFamily: 'Inter', fontWeight: 600}} />
                {chartData.length <= 12 && <Legend iconType="circle" wrapperStyle={{fontSize: '13px', fontWeight: 600, paddingTop: '30px'}} />}
                <Pie data={chartData} cx="50%" cy="50%" innerRadius={80} outerRadius={125} paddingAngle={3} dataKey="value" stroke="none">
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
        <div className="bg-surface border-t border-outline-variant/20 p-8 lg:p-10 flex flex-col gap-6 mt-auto">
           {summary && (
             <div className="flex gap-4 items-start">
                <span className="material-symbols-outlined text-tertiary-container mt-0.5 shrink-0 text-xl font-bold">visibility</span>
                <div>
                   <h4 className="text-[10px] font-extrabold text-slate-500 uppercase tracking-widest mb-1.5 flex items-center gap-2">Data Observation</h4>
                   <p className="text-sm font-semibold text-on-surface leading-relaxed">{summary}</p>
                </div>
             </div>
           )}
           {businessImpact && (
             <div className="flex gap-4 items-start border-t border-outline-variant/10 pt-6">
                <span className="material-symbols-outlined text-primary mt-0.5 shrink-0 text-xl font-bold">bolt</span>
                <div>
                   <h4 className="text-[10px] font-extrabold text-primary uppercase tracking-widest mb-1.5 flex items-center gap-2">Strategic Impact</h4>
                   <p className="text-sm font-bold text-on-surface leading-relaxed">{businessImpact}</p>
                </div>
             </div>
           )}
        </div>
      )}

    </div>
  );
}
