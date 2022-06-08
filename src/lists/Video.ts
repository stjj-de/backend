import { list } from "@keystone-6/core"
import { text, timestamp } from "@keystone-6/core/fields"
import { document } from "@keystone-6/fields-document"
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
    description: document({
      label: "Description",
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
