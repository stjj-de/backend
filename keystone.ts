/*
Welcome to Keystone! This file is what keystone uses to start the app.

It looks at the default export, and expects a Keystone config object.

You can find all the config options in our docs here: https://keystonejs.com/docs/apis/config
*/

import { config } from '@keystone-6/core';

// Look in the schema file for how we define our lists, and how users interact with them through graphql or the Admin UI
import { lists } from './src/schema';

// Keystone auth is configured separately - check out the basic auth setup we are importing from our auth file.
import { withAuth, session } from './src/auth';

export default withAuth(
  config({
    db: {
      provider: 'sqlite',
      url: 'file:./data/keystone.db',
    },
    ui: {
      // For our starter, we check that someone has session data before letting them see the Admin UI.
      isAccessAllowed: (context) => !!context.session?.data,
    },
    lists,
    session,
    images: {
      upload: "local",
      local: {
        baseUrl: "/images",
        storagePath: "./data/uploads/images"
      }
    },
    files: {
      upload: "local",
      local: {
        baseUrl: "/files",
        storagePath: "./data/uploads/files"
      }
    },
    server: {
      port: 8000
    }
  })
);
