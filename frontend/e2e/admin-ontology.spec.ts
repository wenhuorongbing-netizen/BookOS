import { expect, test } from '@playwright/test'
import { login, setSession } from './support/api'

test('admin can dry-run default ontology import without production secrets', async ({ page, request }) => {
  const auth = await login(request, 'admin@bookos.local', 'Admin123!')
  expect(auth.user.role).toBe('ADMIN')
  await setSession(page, auth)

  await page.goto('/admin/ontology')
  await expect(page.getByRole('heading', { name: 'Ontology Seed Import' })).toBeVisible()
  await page.getByRole('button', { name: 'Load Default JSON' }).click()
  await expect(page.getByLabel('Import JSON')).toHaveValue(/Game Design/)
  await page.getByRole('button', { name: 'Dry Run' }).click()
  await expect(page.getByRole('heading', { name: 'Dry Run Result' })).toBeVisible()
  await expect(page.getByText('No records were written.')).toBeVisible()
})
