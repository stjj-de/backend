import { Lists } from '.keystone/types';
import { Post } from "./lists/Post"
import { CustomPage } from "./lists/CustomPage"
import { Video } from "./lists/Video"
import { SettingsSingleton } from "./lists/SettingsSingleton"
import { Picture } from "./lists/Picture"
import { Link } from "./lists/Link"
import { ChurchServiceDate } from "./lists/ChurchServiceDate"
import { Church } from "./lists/Church"
import { Person } from "./lists/Person"
import { User } from "./lists/User"

export const lists: Lists = {
  Church,
  ChurchServiceDate,
  CustomPage,
  Link,
  Person,
  Picture,
  Post,
  SettingsSingleton,
  User,
  Video
};
