/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
        "./*.{js,ts,jsx,tsx}" // Добавь это на всякий случай
    ],
    theme: {
        extend: {},
    },
    plugins: [],
}