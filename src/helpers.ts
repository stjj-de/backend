import { Session } from "./auth"
import { text } from "@keystone-6/core/fields"

export const isAdminPredicate = ({ session }: { session?: Session }) => session?.data?.isAdmin == true
export const isEditorPredicate = ({ session }: { session?: Session }) => session?.data?.id !== undefined

export const isPublishedFilter = ({ session }: { session?: Session }) => isEditorPredicate({ session }) ? {} : { publicationDate: { lte: new Date().toISOString() } }

export const slug = (label: string = "Slug") => text({
  label,
  isIndexed: "unique",
  validation: {
    isRequired: true,
    match: {
      regex: /^[a-z0-9]+(?:-[a-z0-9]+)*$/
    },
    length: {
      max: 100
    }
  },
  access: {
    update: isAdminPredicate
  }
})
