/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        primary: '#0EA5E9',
        accent: '#38BDF8',
        oceanDark: '#0C1A2E',
        softWhite: '#F0F9FF',
      },
      boxShadow: {
        glass: '0 20px 60px rgba(14, 165, 233, 0.22)',
      },
    },
  },
  plugins: [],
};
