import React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {colorIntToHex} from '~/util/modifiers';

import actions from '~/actions/main-function/communicator/communicator-messages';

class CommunicatorMessages extends React.Component {
  constructor(props){
    super(props);
    
    this.touchModeTimeout = null;
    this.firstWasJustSelected = false;
    this.state = {
      touchMode: false
    }
    
    this.toggleMessageSelection = this.toggleMessageSelection.bind(this);
    this.onTouchStartMessage = this.onTouchStartMessage.bind(this);
    this.onTouchEndMessage = this.onTouchEndMessage.bind(this);
    this.onTouchMoveMessage = this.onTouchMoveMessage.bind(this);
    this.onScroll = this.onScroll.bind(this);
    this.checkCanLoadMore = this.checkCanLoadMore.bind(this);
    this.onContextMenu = this.onContextMenu.bind(this);
    
    this.initialXPos = null;
    this.initialYPos = null;
    this.lastXPos = null;
    this.lastYPos = null;
    this.cancelSelection = false;
    this.initialTime = null;
  }
  checkCanLoadMore(){
    if (this.props.communicatorMessages.state === "READY" && this.props.communicatorMessages.hasMore){
      let list = this.refs.list;
      let scrollBottomRemaining = list.scrollHeight - (list.scrollTop + list.offsetHeight)
      if (scrollBottomRemaining <= 100){
        this.props.loadMoreMessages();
      }
    }
  }
  onTouchStartMessage(message, e){
    if (!this.state.touchMode){
      this.touchModeTimeout = setTimeout(()=>{
        this.toggleMessageSelection(message);
        this.firstWasJustSelected = true;
        this.setState({
          touchMode: true
        });
      }, 600);
    }
    this.cancelSelection = false;
    this.initialXPos = e.touches[0].pageX;
    this.initialYPos = e.touches[0].pageY;
    this.initialTime = (new Date()).getTime();
  }
  onTouchMoveMessage(message, e){
    this.lastXPos = e.touches[0].pageX;
    this.lastYPos = e.touches[0].pageY;
    
    if (Math.abs(this.initialXPos - this.lastXPos) >= 5 || Math.abs(this.initialYPos - this.lastYPos) >= 5){
      clearTimeout(this.touchModeTimeout);
      this.cancelSelection = true;
    }
  }
  onTouchEndMessage(message, e){
    clearTimeout(this.touchModeTimeout);
    
    if (this.cancelSelection){
      return;
    }
    
    let currentTime = (new Date()).getTime();
    if (currentTime - this.initialTime <= 300 && !this.state.touchMode){
      this.props.setCurrentMessage(message);
      return;
    }
    
    if (this.state.touchMode && !this.firstWasJustSelected){
      let isSelected = this.toggleMessageSelection(message);
      if (isSelected && this.props.communicatorMessages.selectedIds.length === 1){
        this.setState({
          touchMode: false
        });
      }
    } else if (this.firstWasJustSelected){
      this.firstWasJustSelected = false;
    }
  }
  onContextMenu(e){
    e.preventDefault();
    e.stopPropagation();
  }
  toggleMessageSelection(message){
    let isSelected = this.props.communicatorMessages.selectedIds.includes(message.communicatorMessageId);
    if (isSelected){
      this.props.removeFromCommunicatorSelectedMessages(message)
    } else {
      this.props.addToCommunicatorSelectedMessages(message);
    }
    return isSelected;
  }
  componentWillReceiveProps(nextProps){
    if (nextProps.communicatorMessages.state === "LOADING"){
      this.setState({
        touchMode: false
      });
    }
  }
  componentDidUpdate(){
    if (this.props.communicatorMessages.state === "READY" && this.props.communicatorMessages.hasMore){
      let list = this.refs.list;
      let doesNotHaveScrollBar = list.scrollHeight === list.offsetHeight;
      if (doesNotHaveScrollBar){
        this.props.loadMoreMessages();
      }
    }
    this.checkCanLoadMore();
  }
  onScroll(e){
    this.checkCanLoadMore();
  }
  render(){
    if (this.props.communicatorMessages.state === "LOADING"){
      return null;
    } else if (this.props.communicatorMessages.state === "ERROR"){
      //TODO: put a translation here please! this happens when messages fail to load, a notification shows with the error
      //message but here we got to put something
      return <div className="empty"><span>{"ERROR"}</span></div>
    } else if (this.props.communicatorMessages.messages.length === 0){
      return <div className="empty"><span>{this.props.i18n.text.get("plugin.communicator.empty.topic")}</span></div>
    }
    
    return <div className={`communicator application-list ${this.state.touchMode ? "application-list-select-mode" : ""}`}
     ref="list" onScroll={this.onScroll}>{
      this.props.communicatorMessages.messages.map((message, index)=>{
        let isSelected = this.props.communicatorMessages.selectedIds.includes(message.communicatorMessageId);
        return <div key={message.communicatorMessageId}
          className={`application-list-item ${message.unreadMessagesInThread ? "communicator-application-list-item-unread" : ""} ${isSelected ? "selected" : ""}`}
          onTouchStart={this.onTouchStartMessage.bind(this, message)} onTouchEnd={this.onTouchEndMessage.bind(this, message)}
          onTouchMove={this.onTouchMoveMessage.bind(this, message)} onClick={this.props.setCurrentMessage.bind(null, message)}
          onContextMenu={this.onContextMenu}>
          <div className="application-list-item-header">
            <input type="checkbox" checked={isSelected} onChange={this.toggleMessageSelection.bind(this, message)}/>
            <span className="communicator text communicator-text-username">
              {message.sender.firstName ? message.sender.firstName + " " : ""}{message.sender.lastName ? message.sender.lastName : ""}
            </span>
            <span className="communicator-application-list-item-labels">{message.labels.map((label)=>{
              return <span className="communicator text communicator-text-tag" key={label.id}>
                <span className="icon icon-tag" style={{color: colorIntToHex(label.labelColor)}}></span>
                {label.labelName}
              </span>
            })}</span>
            {message.messageCountInthread ? <span className="communicator text communicator-text-counter">
              {message.messageCountInthread}
            </span> : null}
            <span className="communicator text communicator-text-date">
              {this.props.i18n.time.format(message.threadLatestMessageDate)}
            </span>
          </div>
          <div className="application-list-item-body">
            <span className="communicator text communicator-text-body">{message.caption}</span>
          </div>
        </div>
      })
    }{
      this.props.communicatorMessages.state === "LOADING_MORE" ? 
        <div className="application-list-item loader-empty"/>
    : null}</div>
  }
}

function mapStateToProps(state){
  return {
    communicatorMessages: state.communicatorMessages,
    i18n: state.i18n
  }
};

const mapDispatchToProps = (dispatch)=>{
  return bindActionCreators(actions, dispatch);
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(CommunicatorMessages);