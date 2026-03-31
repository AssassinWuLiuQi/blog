import JSEncrypt from 'jsencrypt'

let publicKey: string | null = null

export function setPublicKey(key: string): void {
  publicKey = key
}

export function encrypt(plainText: string): string {
  if (!publicKey) {
    throw new Error('Public key not set. Call setPublicKey first.')
  }
  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(publicKey)
  return encryptor.encrypt(plainText) as string
}

export function getPublicKey(): string | null {
  return publicKey
}
