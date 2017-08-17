import PropTypes from 'prop-types';
import React from 'react';
import {connect} from 'react-redux';

class Feed extends React.Component {
  static propTypes = {
    entries: PropTypes.arrayOf(PropTypes.shape({
      publicationDate: PropTypes.string.isRequired,
      description: PropTypes.string.isRequired,
      link: PropTypes.string.isRequired,
      title: PropTypes.string.isRequired
    })).isRequired
  }
  render(){
    return <ul className="feed">
      {this.props.entries.map((entry, index)=>{
        return <li className="feed-item">
          <span className="feed-item-description">
            <a href={entry.link} target="top">{entry.title}</a>
            {entry.description}
          </span>
          <span className="feed-item-date">{this.props.i18n.time.format(entry.publicationDate)}</span>
        </li>
      })}
    </ul>
  }
}

function mapStateToProps(state){
  return {
    i18n: state.i18n
  }
};

const mapDispatchToProps = (dispatch)=>{
  return {};
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Feed);