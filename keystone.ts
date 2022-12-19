import { config } from '@keystone-6/core';
import { lists } from './src/schema';
import { withAuth, session } from './src/auth';

export default withAuth(
  config({
    db: {
      provider: 'sqlite',
      url: 'file:./data/keystone.db',
    },
    ui: {
      isAccessAllowed: (context) => !!context.session?.data,
      isDisabled: false
    },
    lists,
    session,
    storage: {
      files: {
        type: "file",
        kind: "local",
        storagePath: "./data/uploads/files",
        serverRoute: {
          path: "/files"
        },
        generateUrl: path => `/files${path}`
      },
      images: {
        type: "image",
        kind: "local",
        storagePath: "./data/uploads/images",
        serverRoute: {
          path: "/images"
        },
        generateUrl: path => `/images${path}`
      }
    },
    server: {
      port: 8000
    }
  })
);
