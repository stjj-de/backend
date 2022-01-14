module.exports = ({ env }) => ({
  auth: {
    secret: env('ADMIN_JWT_SECRET', '441870f95a1fe2da5be6cfb0a1821a16'),
  },
});
