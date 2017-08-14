import App from './containers/index.jsx';
import reducer from './reducers/index';

import {logger} from './debug/redux-logger';
let store = Redux.createStore(reducer, Redux.applyMiddleware(logger));
//let store = Redux.createStore(reducer);

let Provider = ReactRedux.Provider;

ReactDOM.render(<Provider store={store}>
  <App/>
</Provider>, document.querySelector("#app"));