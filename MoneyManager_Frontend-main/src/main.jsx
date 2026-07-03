import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import './index.css'
import {AppContextProvider} from "./context/AppContext.jsx";
import {QueryClientProvider} from "@tanstack/react-query";
import queryClient from "./util/queryClient.js";

ReactDOM.createRoot(document.getElementById('root')).render(
  <QueryClientProvider client={queryClient}>
    <AppContextProvider>
      <App />
    </AppContextProvider>
  </QueryClientProvider>
)

