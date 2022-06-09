import { list } from "@keystone-6/core"
import { checkbox, relationship, text, timestamp } from "@keystone-6/core/fields"
import { document } from "@keystone-6/fields-document"
import { isEditorPredicate } from "../helpers"

export const ChurchServiceDate = list({
  fields: {
    shortDescription: text({
      label: "Short description",
      validation: {
        isRequired: true,
        length: {
          max: 100
        }
      }
    }),
    longDescription: document({
      label: "Long description",
      formatting: {
        inlineMarks: {
          bold: true,
          italic: true,
          superscript: true,
          strikethrough: true
        }
      },
      links: true
    }),
    date: timestamp({
      label: "Date",
      validation: {
        isRequired: true
      }
    }),
    livestreamPlanned: checkbox({
      label: "Livestream is planned",
      defaultValue: false
    }),
    church: relationship({
      label: "Church",
      ref: "Church",
      many: false
    })
  },
  access: {
    operation: {
      create: isEditorPredicate,
      query: async ({ context }) => {
        const filter = { date: { lte: new Date().toISOString() } }

        await context.prisma.ChurchServiceDate.deleteMany({ where: filter })

        return true
      },
      update: isEditorPredicate,
      delete: isEditorPredicate
    }
  },
  ui: {
    listView: {
      initialColumns: ["date", "shortDescription", "livestreamPlanned"]
    },
    labelField: "shortDescription"
  },
  hooks: {
    validateInput({ addValidationError, resolvedData }) {
      if (resolvedData.date.toISOString() < new Date().toISOString()) {
        addValidationError("Date must be in the future")
      }
    }
  }
})
