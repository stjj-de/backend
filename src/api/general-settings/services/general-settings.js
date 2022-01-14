'use strict';

/**
 * general-settings service.
 */

const { createCoreService } = require('@strapi/strapi').factories;

module.exports = createCoreService('api::general-settings.general-settings');
