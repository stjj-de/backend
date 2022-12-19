import { list } from "@keystone-6/core"
import { checkbox, relationship, text, timestamp } from "@keystone-6/core/fields"
import { isEditorPredicate, isPublishedFilter, slug } from "../helpers"

export const Post = list({
  fields: {
    title: text({
      label: "Title",
      validation: {
        isRequired: true
      }
    }),
    publicationDate: timestamp({
      label: "Publication date",
      validation: {
        isRequired: false
      }
    }),
    slug: slug(),
    authors: relationship({
      label: "Authors",
      ref: "Person",
      many: true
    }),
    featured: checkbox({
      label: "Is featured"
    }),
    content: text()
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
  },
  ui: {
    listView: {
      initialColumns: ["title", "authors"],
      initialSort: {
        field: "publicationDate",
        direction: "DESC"
      }
    },
    labelField: "title",
    searchFields: ["title", "slug"]
  }
})
