import { useEffect, useState } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import { getCharts, getInsights, generateFlowchart } from '../services/api';
import { Loader2, AlertTriangle, TrendingUp, AlertCircle } from 'lucide-react';
import { BarChart, Bar, LineChart, Line, PieChart, Pie, ScatterChart, Scatter, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, Cell } from 'recharts';
import mermaid from 'mermaid';

// Initialize mermaid
mermaid.initialize({ startOnLoad: false, theme: 'default' });

export default function DashboardPage() {
  const { datasetId } = useParams();
  const location = useLocation();
  const activeTab = location.pathname.split('/').pop() || 'dashboard'; // 'dashboard' (charts), 'insights', 'flowchart'

  const [loading, setLoading] = useState(true);
  const [data, setData] = useState({ charts: [], insights: null, flowchart: null });
  const [error, setError] = useState('');

  // Fetch logic based on tab
  useEffect(() => {
    let mounted = true;
    
    const loadData = async () => {
      setLoading(true);
      setError('');
      try {
        if (activeTab === 'insights') {
          if (!data.insights) {
             const res = await getInsights(datasetId);
             if(mounted) setData(prev => ({ ...prev, insights: res }));
          }
        } else if (activeTab === 'flowchart') {
          if (!data.flowchart) {
            const res = await generateFlowchart(datasetId);
            if(mounted) {
              setData(prev => ({ ...prev, flowchart: res }));
            }
          }
        } else {
          // default charts
          if (data.charts.length === 0) {
            const res = await getCharts(datasetId);
            if(mounted) setData(prev => ({ ...prev, charts: res }));
          }
        }
      } catch (err) {
        console.error(err);
        if(mounted) setError('Failed to load data. Is the backend running?');
      } finally {
        if(mounted) setLoading(false);
      }
    };

    loadData();
    return () => mounted = false;
  }, [datasetId, activeTab]);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center p-20 text-slate-400">
        <Loader2 size={48} className="animate-spin mb-4 text-indigo-500" />
        <p className="font-medium text-lg">Analyzing Data...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-8 bg-red-50 border border-red-200 rounded-2xl text-red-600 flex items-center gap-3">
        <AlertTriangle size={24} />
        <span className="font-medium">{error}</span>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-900 capitalize">
          {activeTab === datasetId ? 'Data Visualizations' : activeTab}
        </h1>
        <p className="text-slate-500 mt-2">Displaying analysis for dataset #{datasetId}</p>
      </div>

      {activeTab === 'insights' && data.insights && <InsightsTab insights={data.insights} />}
      {activeTab === 'flowchart' && data.flowchart && <FlowchartTab flowchart={data.flowchart} />}
      {(activeTab === 'dashboard' || activeTab === datasetId) && <ChartsTab charts={data.charts} />}
      
    </div>
  );
}

// ----- TAB COMPONENTS -----

function ChartsTab({ charts }) {
  if (!charts || charts.length === 0) return <p className="text-slate-500 p-8 text-center bg-white rounded-2xl">No charts could be generated for this dataset.</p>;

  // A small color palette for charts without defined colors
  const COLORS = ['#4f46e5', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981'];

  return (
    <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
      {charts.map((chart, idx) => {
        const { type, title, config } = chart;
        const cConfig = config || {};
        
        // Recharts needs an array of objects. We map the backend config.labels and datasets to Recharts format.
        let chartData = [];
        if (cConfig.labels && cConfig.datasets && cConfig.datasets[0]) {
          chartData = cConfig.labels.map((label, i) => ({
            name: label,
            value: cConfig.datasets[0].data[i]
          }));
        } else if (type === 'scatter' && cConfig.datasets && cConfig.datasets[0]) {
            // Scatter data is already {x, y}
            chartData = cConfig.datasets[0].data;
        }

        return (
          <div key={idx} className="bg-white p-6 rounded-3xl shadow-sm border border-slate-200 h-[400px] flex flex-col">
            <h3 className="font-bold text-slate-800 mb-6">{title}</h3>
            <div className="flex-1 w-full min-h-0">
              <ResponsiveContainer width="100%" height="100%">
                {type === 'bar' ? (
                  <BarChart data={chartData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                    <XAxis dataKey="name" tick={{fontSize: 12, fill: '#64748b'}} />
                    <YAxis tick={{fontSize: 12, fill: '#64748b'}} />
                    <Tooltip cursor={{fill: '#f8fafc'}} contentStyle={{borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)'}} />
                    <Bar dataKey="value" fill="#4f46e5" radius={[4, 4, 0, 0]} />
                  </BarChart>
                ) : type === 'line' ? (
                  <LineChart data={chartData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                    <XAxis dataKey="name" tick={{fontSize: 12, fill: '#64748b'}} />
                    <YAxis tick={{fontSize: 12, fill: '#64748b'}} />
                    <Tooltip contentStyle={{borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)'}} />
                    <Line type="monotone" dataKey="value" stroke="#8b5cf6" strokeWidth={3} activeDot={{ r: 8 }} />
                  </LineChart>
                ) : type === 'pie' ? (
                  <PieChart>
                    <Tooltip contentStyle={{borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)'}} />
                    <Legend iconType="circle" wrapperStyle={{fontSize: '12px'}} />
                    <Pie data={chartData} cx="50%" cy="50%" innerRadius={60} outerRadius={100} paddingAngle={5} dataKey="value">
                      {chartData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                  </PieChart>
                ) : type === 'scatter' ? (
                  <ScatterChart margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                    <XAxis dataKey="x" type="number" tick={{fontSize: 12, fill: '#64748b'}} />
                    <YAxis dataKey="y" type="number" tick={{fontSize: 12, fill: '#64748b'}} />
                    <Tooltip cursor={{strokeDasharray: '3 3'}} contentStyle={{borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)'}} />
                    <Scatter data={chartData} fill="#ec4899" />
                  </ScatterChart>
                ) : (
                   <p>Unsupported chart type</p>
                )}
              </ResponsiveContainer>
            </div>
          </div>
        );
      })}
    </div>
  );
}

function InsightsTab({ insights }) {
  const details = insights.details || {};
  const trends = details.trends || [];
  const anomalies = details.anomalies || [];
  const explanation = details.explanation || insights.summary;

  return (
    <div className="space-y-6">
      {/* Summary Card */}
      <div className="bg-indigo-600 text-white p-8 rounded-3xl shadow-lg shadow-indigo-500/20">
        <h2 className="text-xl font-bold mb-4 opacity-90">Executive Summary</h2>
        <p className="text-lg leading-relaxed">{explanation}</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Trends */}
        <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm">
          <div className="flex items-center gap-2 mb-6 text-emerald-600">
            <TrendingUp size={24} />
            <h3 className="text-xl font-bold text-slate-800">Key Trends</h3>
          </div>
          <ul className="space-y-4">
             {trends.map((trend, i) => (
               <li key={i} className="flex gap-3 text-slate-600 bg-slate-50 p-4 rounded-2xl">
                 <div className="w-1.5 h-1.5 rounded-full bg-emerald-500 mt-2 shrink-0"></div>
                 {trend}
               </li>
             ))}
             {trends.length === 0 && <p className="text-slate-400">No major trends detected.</p>}
          </ul>
        </div>

        {/* Anomalies */}
        <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm">
          <div className="flex items-center gap-2 mb-6 text-amber-500">
            <AlertCircle size={24} />
            <h3 className="text-xl font-bold text-slate-800">Anomalies Detected</h3>
          </div>
          <ul className="space-y-4">
             {anomalies.map((anomaly, i) => (
               <li key={i} className="flex gap-3 text-slate-600 bg-amber-50 rounded-2xl p-4 border border-amber-100">
                 <div className="w-1.5 h-1.5 rounded-full bg-amber-500 mt-2 shrink-0"></div>
                 {anomaly}
               </li>
             ))}
             {anomalies.length === 0 && <p className="text-slate-400">No anomalies detected.</p>}
          </ul>
        </div>
      </div>
    </div>
  );
}

function FlowchartTab({ flowchart }) {
  const [svgContent, setSvgContent] = useState('');
  
  useEffect(() => {
    let isMounted = true;
    const structure = flowchart.structure;
    if (!structure || !structure.nodes || !structure.edges) return;

    // Convert backend JSON nodes/edges to Mermaid string
    const nodeDefs = structure.nodes.map(n => {
      const shapeStart = n.type === 'process' ? '[' : n.type === 'data' ? '[(' : '(';
      const shapeEnd = n.type === 'process' ? ']' : n.type === 'data' ? ')]' : ')';
      return `    ${n.id}${shapeStart}"${n.label}"${shapeEnd}`;
    });
    
    const edgeDefs = structure.edges.map(e => {
       const labelStr = e.label ? `|"${e.label}"|` : '-->';
       const edgeStr = e.label ? `--> ${labelStr}` : '-->'; 
       return `    ${e.source} ${edgeStr} ${e.target}`;
    });

    const graphDef = `
graph TD
${nodeDefs.join('\n')}
${edgeDefs.join('\n')}
    `;

    const renderMermaid = async () => {
      try {
        const { svg } = await mermaid.render('mermaid-chart', graphDef);
        if (isMounted) setSvgContent(svg);
      } catch (e) {
        console.error("Mermaid parsing failed", e);
      }
    };
    renderMermaid();

    return () => isMounted = false;
  }, [flowchart]);

  return (
    <div className="bg-white p-8 rounded-3xl border border-slate-200 shadow-sm flex flex-col items-center">
      <h2 className="text-xl font-bold text-slate-800 mb-6 self-start">Data Processing Flow</h2>
      <div className="w-full max-w-3xl overflow-x-auto bg-slate-50 p-8 rounded-2xl flex justify-center border border-slate-100">
        {svgContent ? (
          <div dangerouslySetInnerHTML={{ __html: svgContent }} className="min-w-full flex justify-center [&>svg]:max-w-full [&>svg]:h-auto" />
        ) : (
          <p className="text-slate-400 py-20 flex items-center gap-2"><Loader2 className="animate-spin" /> Rendering Workflow...</p>
        )}
      </div>
      <p className="text-sm text-slate-500 mt-6">{flowchart.structure?.description}</p>
    </div>
  );
}
