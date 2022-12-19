import { list } from "@keystone-6/core"
import { json, text, timestamp } from "@keystone-6/core/fields"
import { isEditorPredicate, isPublishedFilter } from "../helpers"

export const Video = list({
  fields: {
    title: text({
      label: "Title",
      validation: {
        isRequired: true
      }
    }),
    youtubeVideoId: text({
      label: "YouTube Video ID",
      validation: {
        isRequired: true
      },
      isOrderable: false,
      isFilterable: false
    }),
    publicationDate: timestamp({
      label: "Publication date",
      validation: {
        isRequired: true
      }
    }),
    description: json({
      isFilterable: false,
      isOrderable: false,
      defaultValue: {}
    })
  },
  access: {
    operation: {
      create: isEditorPredicate,
      query: () => true,
      update: isEditorPredicate,
      delete: isEditorPredicate
    },
    filter: {
      query: isPublishedFilter
    }
  }
})
