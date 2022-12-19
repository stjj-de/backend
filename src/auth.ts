import { createAuth } from '@keystone-6/auth';

import { statelessSessions } from '@keystone-6/core/session';

let sessionSecret = process.env.SESSION_SECRET;

if (!sessionSecret) {
  if (process.env.NODE_ENV === 'production') {
    throw new Error(
      'The SESSION_SECRET environment variable must be set in production'
    );
  } else {
    sessionSecret = '-- DEV COOKIE SECRET; CHANGE ME --';
  }
}

const { withAuth } = createAuth({
  listKey: 'User',
  identityField: 'username',
  secretField: 'password',
  sessionData: "id username isAdmin",
  initFirstItem: {
    fields: ['firstName', 'username', 'password'],
    itemData: { isAdmin: true },
    skipKeystoneWelcome: true
  }
})

const session = statelessSessions({
  maxAge: 60 * 60 * 24 * 60, // 60 days,
  secret: sessionSecret!,
});

export interface Session {
  data: {
    id: string
    username: string
    isAdmin: boolean
  }
}

export { withAuth, session };
