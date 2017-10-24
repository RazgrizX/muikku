export interface SpecificActionType<ActionType, PayloadType> {
  type: ActionType,
  payload: PayloadType | null
}

type dispatch = (action:any)=>any;
type getState = ()=>any;
type AsyncDeferredAction = (dispatch:(arg:AnyActionType)=>any, getState:()=>any)=>Promise<void>;
type DeferredAction = (dispatch:(arg:AnyActionType)=>any, getState:()=>any)=>any;

import {SET_LOCALE} from './base/locales';
import {ADD_NOTIFICATION, HIDE_NOTIFICATION} from './base/notifications';
import {LOGOUT} from './base/status';
import {UPDATE_TITLE} from './base/title';
import {SET_CURRENT_THREAD, UPDATE_MESSAGES_STATE, UPDATE_MESSAGES_ALL_PROPERTIES,
  UPDATE_MESSAGE_ADD_LABEL, UPDATE_MESSAGE_DROP_LABEL, PUSH_ONE_MESSAGE_FIRST, LOCK_TOOLBAR, UNLOCK_TOOLBAR,
  UPDATE_ONE_MESSAGE, DELETE_MESSAGE, UPDATE_SIGNATURE, UPDATE_SELECTED_MESSAGES, ADD_TO_COMMUNICATOR_SELECTED_MESSAGES,
  REMOVE_FROM_COMMUNICATOR_SELECTED_MESSAGES} from './main-function/communicator/communicator-messages';
import {UPDATE_COMMUNICATOR_NAVIGATION_LABELS, ADD_COMMUNICATOR_NAVIGATION_LABEL,
  UPDATE_ONE_LABEL_FROM_ALL_MESSAGES, UPDATE_COMMUNICATOR_NAVIGATION_LABEL,
  DELETE_COMMUNICATOR_NAVIGATION_LABEL, REMOVE_ONE_LABEL_FROM_ALL_MESSAGES} from './main-function/communicator/communicator-navigation';
import {UPDATE_ANNOUNCEMENTS} from './main-function/announcements';
import {UPDATE_LAST_MESSAGES} from './main-function/last-messages';
import {UPDATE_MESSAGE_COUNT} from './main-function/message-count';
import {UPDATE_LAST_WORKSPACE} from './main-function/last-workspace';
import {UPDATE_WORKSPACES} from './main-function/workspaces';
import {UPDATE_ANNOUNCEMENTS_STATE, UPDATE_ANNOUNCEMENTS_ALL_PROPERTIES, UPDATE_SELECTED_ANNOUNCEMENTS,
  ADD_TO_ANNOUNCEMENTS_SELECTED, REMOVE_FROM_ANNOUNCEMENTS_SELECTED,
  UPDATE_ONE_ANNOUNCEMENT, DELETE_ANNOUNCEMENT } from '~/actions/main-function/announcer/announcements';
import {UPDATE_DISCUSSION_AREAS, PUSH_DISCUSSION_AREA_LAST, UPDATE_DISCUSSION_AREA} from './main-function/discussion/discussion-areas';
import {UPDATE_DISCUSSION_THREADS_STATE, UPDATE_DISCUSSION_THREADS_ALL_PROPERTIES} from './main-function/discussion/discussion-threads';

interface WEBSOCKET_EVENT extends SpecificActionType<"WEBSOCKET_EVENT", {event: string}> {};

export type ActionType = SET_CURRENT_THREAD | UPDATE_MESSAGES_STATE |
UPDATE_MESSAGES_ALL_PROPERTIES | UPDATE_MESSAGE_ADD_LABEL | UPDATE_MESSAGE_DROP_LABEL | PUSH_ONE_MESSAGE_FIRST |
LOCK_TOOLBAR | UNLOCK_TOOLBAR | UPDATE_ONE_MESSAGE | DELETE_MESSAGE | UPDATE_MESSAGES_ALL_PROPERTIES | UPDATE_SIGNATURE |
SET_LOCALE | ADD_NOTIFICATION | HIDE_NOTIFICATION | LOGOUT | UPDATE_TITLE | UPDATE_SELECTED_MESSAGES | ADD_TO_COMMUNICATOR_SELECTED_MESSAGES |
REMOVE_FROM_COMMUNICATOR_SELECTED_MESSAGES | UPDATE_MESSAGE_COUNT | UPDATE_LAST_WORKSPACE | UPDATE_WORKSPACES | UPDATE_LAST_MESSAGES |
UPDATE_ANNOUNCEMENTS | UPDATE_COMMUNICATOR_NAVIGATION_LABELS | ADD_COMMUNICATOR_NAVIGATION_LABEL | UPDATE_ONE_LABEL_FROM_ALL_MESSAGES
| UPDATE_COMMUNICATOR_NAVIGATION_LABEL | DELETE_COMMUNICATOR_NAVIGATION_LABEL | REMOVE_ONE_LABEL_FROM_ALL_MESSAGES | WEBSOCKET_EVENT |
UPDATE_ANNOUNCEMENTS_STATE | UPDATE_ANNOUNCEMENTS_ALL_PROPERTIES | UPDATE_SELECTED_ANNOUNCEMENTS | ADD_TO_ANNOUNCEMENTS_SELECTED |
REMOVE_FROM_ANNOUNCEMENTS_SELECTED | UPDATE_ONE_ANNOUNCEMENT | DELETE_ANNOUNCEMENT | UPDATE_DISCUSSION_AREAS |
UPDATE_DISCUSSION_THREADS_STATE | UPDATE_DISCUSSION_THREADS_ALL_PROPERTIES | PUSH_DISCUSSION_AREA_LAST | UPDATE_DISCUSSION_AREA |
DELETE_DISCUSSION_AREA
export type AnyActionType = ActionType | DeferredAction | AsyncDeferredAction