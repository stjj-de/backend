import { list } from "@keystone-6/core"
import { checkbox, relationship, text, timestamp } from "@keystone-6/core/fields"
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
    longDescription: text(),
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
      query: () => true,
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
      if (resolvedData.date !== undefined && resolvedData.date.toISOString() < new Date().toISOString()) {
        addValidationError("Date must be in the future")
      }
    },
    async afterOperation({ operation, context }) {
      if (operation === "create") {
        // delete old dates
        const filter = { date: { lte: new Date().toISOString() } }
        await context.prisma.ChurchServiceDate.deleteMany({ where: filter })
      }
    }
  }
})
