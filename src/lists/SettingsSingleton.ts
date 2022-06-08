import { list } from "@keystone-6/core"
import { file, relationship, text } from "@keystone-6/core/fields"
import { document } from "@keystone-6/fields-document"
import { isAdminPredicate, isEditorPredicate } from "../helpers"

export const SettingsSingleton = list({
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
      label: "Parish bulletin"
    }),
    acolyteSchedule: file({
      label: "Acolyte schedule"
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
    officeSectionContent: document({
      label: "»Pfarrbüro« section",
      formatting: {
        listTypes: true,
        blockTypes: {
          blockquote: true
        },
        headingLevels: [2, 3],
        inlineMarks: {
          bold: true,
          italic: true,
          superscript: true,
          strikethrough: true
        }
      },
      links: true
    }),
    mediaLibraryPageContent: document({
      label: "»Mediathek« page",
      formatting: {
        listTypes: true,
        blockTypes: {
          blockquote: true
        },
        headingLevels: [1, 2, 3],
        inlineMarks: {
          bold: true,
          italic: true,
          superscript: true,
          strikethrough: true
        }
      },
      links: true
    })
  },
  access: {
    operation: {
      create: async ({ session, context }) => isAdminPredicate({ session }) && (await context.db.SettingsSingleton.count()) == 0,
      query: () => true,
      update: isEditorPredicate,
      delete: () => false
    }
  },
  ui: {
    listView: {
      initialColumns: ["id"],
      pageSize: 1
    },
    hideDelete: () => true,
    hideCreate: async ({ session, context }) => !(isAdminPredicate({ session }) && (await context.db.SettingsSingleton.count()) == 0)
  }
})
