import PropTypes from 'prop-types';
import Dropdown from '../dropdown.jsx';
import Link from '../link.jsx';
import React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';

import actions from '../../../actions/base/status';

class ProfileItem extends React.Component {
  static propTypes = {
    classNameExtension: PropTypes.string.isRequired,
  }
  render(){
    if (!this.props.status.loggedIn){
      return null;
    }
    const items = [
      {
        icon: "user",
        text: 'plugin.profile.links.personal',
        href: "/profile"
      },
      {
        icon: "forgotpassword",
        text: 'plugin.footer.instructions'
      },
      {
        icon: "helpdesk",
        text: 'plugin.home.helpdesk'
      },
      {
        icon: "signout",
        text: 'plugin.logout.logout',
        onClick: this.props.logout
      }
    ]
    return <Dropdown classNameExtension={this.props.classNameExtension} classNameSuffix="profile-menu" items={items.map((item)=>{
        return (closeDropdown)=>{return <Link href="/profile"
         className={`${this.props.classNameExtension} link link-full ${this.props.classNameExtension}-link-profile-menu`}
         onClick={(...args)=>{closeDropdown();item.onClick && item.onClick(...args)}} href={item.href}>
          <span className={`icon icon-${item.icon}`}></span>
          <span>{this.props.i18n.text.get(item.text)}</span>
        </Link>}
      })}>
      <a className="main-function button-pill main-function-button-pill-profile">
        <object className="embbed embbed-full"
         data={`/rest/user/files/user/${this.props.status.userId}/identifier/profile-image-96`}
         type="image/jpeg">
          <span className="icon icon-user"></span>
        </object>
      </a>
    </Dropdown>
  }
}

function mapStateToProps(state){
  return {
    i18n: state.i18n,
    status: state.status
  }
};

const mapDispatchToProps = (dispatch)=>{
  return bindActionCreators(actions, dispatch);
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ProfileItem);