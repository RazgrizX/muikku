import notificationActions from '~/actions/base/notifications';
import promisify from '~/util/promisify';
import mApi from '~/lib/mApi';
import {AnyActionType, SpecificActionType} from '~/actions';
import {AnnouncementsStateType, AnnouncementsPatchType,
  AnnouncementListType, AnnouncementType, AnnouncementUpdateType} from '~/reducers/main-function/announcer/announcements';
import { loadAnnouncementsHelper } from './announcements/helpers';

export interface UPDATE_ANNOUNCEMENTS_STATE extends SpecificActionType<"UPDATE_ANNOUNCEMENTS_STATE", AnnouncementsStateType>{}
export interface UPDATE_ANNOUNCEMENTS_ALL_PROPERTIES extends SpecificActionType<"UPDATE_ANNOUNCEMENTS_ALL_PROPERTIES", AnnouncementsPatchType>{}
export interface UPDATE_SELECTED_ANNOUNCEMENTS extends SpecificActionType<"UPDATE_SELECTED_ANNOUNCEMENTS", AnnouncementListType>{}
export interface ADD_TO_ANNOUNCEMENTS_SELECTED extends SpecificActionType<"ADD_TO_ANNOUNCEMENTS_SELECTED", AnnouncementType>{}
export interface REMOVE_FROM_ANNOUNCEMENTS_SELECTED extends SpecificActionType<"REMOVE_FROM_ANNOUNCEMENTS_SELECTED", AnnouncementType>{}
export interface UPDATE_ONE_ANNOUNCEMENT extends SpecificActionType<"UPDATE_ONE_ANNOUNCEMENT", {
  update: AnnouncementUpdateType,
  announcement: AnnouncementType
}>{}
export interface DELETE_ANNOUNCEMENT extends SpecificActionType<"DELETE_ANNOUNCEMENT", AnnouncementType>{}

export interface LoadAnnouncementsTriggerType {
  (location:string, workspaceId?:number):AnyActionType
}

export interface AddToAnnouncementsSelectedTriggerType {
  (announcement: AnnouncementType):AnyActionType
}

export interface RemoveFromAnnouncementsSelectedTriggerType {
  (announcement: AnnouncementType):AnyActionType
}

export interface UpdateAnnouncementTriggerType {
  (announcement: AnnouncementType, update: AnnouncementUpdateType):AnyActionType
}

let loadAnnouncements:LoadAnnouncementsTriggerType = function loadAnnouncements(location, workspaceId){
  return loadAnnouncementsHelper.bind(this, location, workspaceId);
}

let addToAnnouncementsSelected:AddToAnnouncementsSelectedTriggerType = function addToAnnouncementsSelected(announcement){
  return {
    type: "ADD_TO_ANNOUNCEMENTS_SELECTED",
    payload: announcement
  }
}

let removeFromAnnouncementsSelected:AddToAnnouncementsSelectedTriggerType = function removeFromAnnouncementsSelected(announcement){
  return {
    type: "REMOVE_FROM_ANNOUNCEMENTS_SELECTED",
    payload: announcement
  }
}

let updateAnnouncement:UpdateAnnouncementTriggerType = function(announcement, update){
  return  (dispatch:(arg:AnyActionType)=>any, getState:()=>any)=>{
    //TODO
  }
}