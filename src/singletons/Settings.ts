import { list } from "@keystone-6/core"
import { file, relationship, text } from "@keystone-6/core/fields"
import { isAdminPredicate, isEditorPredicate } from "../helpers"

export const Settings = list({
  graphql: {
    plural: "PluralSettings"
  },
  isSingleton: true,
  fields: {
    livestreamVideoId: text({
      label: "Livestream video ID",
      validation: {
        match: {
          regex: /^[a-zA-Z0-9_-]*$/,
          explanation: "YouTube Video ID"
        },
        isRequired: false
      }
    }),
    parishBulletin: file({
      label: "Parish bulletin",
      storage: "files"
    }),
    acolyteSchedule: file({
      label: "Acolyte schedule",
      storage: "files"
    }),
    pastors: relationship({
      label: "Pastors",
      ref: "Person",
      many: true
    }),
    homePageLinks: relationship({
      label: "Hero links",
      ref: "Link",
      many: true
    }),
    footerLinks: relationship({
      label: "Footer links",
      ref: "Link",
      many: true
    }),
    officeSectionContent: text(),
    mediaLibraryPageContent: text()
  },
  access: {
    operation: {
      create: () => true,
      query: () => true,
      update: isEditorPredicate,
      delete: () => false
    }
  }
})
