/** @type {import('tailwindcss').Config} */
export default {
    content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
    darkMode: 'class',
    theme: {
        extend: {
            colors: {
                primary: '#3b82f6',
                'background-light': '#ffffff',
                'background-dark': '#111827',
            },
            fontFamily: {
                sans: ['Inter', 'sans-serif'],
            },
            typography: {
                DEFAULT: {
                    css: {
                        maxWidth: 'none',
                        color: 'inherit',
                        a: {
                            color: '#3b82f6',
                            textDecoration: 'underline',
                            fontWeight: '500',
                        },
                        'a:hover': {
                            color: '#1d4ed8',
                        },
                        strong: {
                            color: 'inherit',
                            fontWeight: '600',
                        },
                        'ol[type="A"]': {
                            '--list-counter-style': 'upper-alpha',
                        },
                        'ol[type="a"]': {
                            '--list-counter-style': 'lower-alpha',
                        },
                        'ol[type="A" s]': {
                            '--list-counter-style': 'upper-alpha',
                        },
                        'ol[type="a" s]': {
                            '--list-counter-style': 'lower-alpha',
                        },
                        'ol[type="I"]': {
                            '--list-counter-style': 'upper-roman',
                        },
                        'ol[type="i"]': {
                            '--list-counter-style': 'lower-roman',
                        },
                        'ol[type="I" s]': {
                            '--list-counter-style': 'upper-roman',
                        },
                        'ol[type="i" s]': {
                            '--list-counter-style': 'lower-roman',
                        },
                        'ol[type="1"]': {
                            '--list-counter-style': 'decimal',
                        },
                        'ol > li': {
                            position: 'relative',
                        },
                        'ol > li::marker': {
                            fontWeight: '400',
                            color: 'rgb(107 114 128)',
                        },
                        'ul > li': {
                            position: 'relative',
                        },
                        'ul > li::marker': {
                            color: 'rgb(107 114 128)',
                        },
                        hr: {
                            borderColor: 'rgb(229 231 235)',
                            borderTopWidth: 1,
                        },
                        blockquote: {
                            fontWeight: '500',
                            fontStyle: 'italic',
                            color: 'rgb(107 114 128)',
                            borderLeftWidth: '0.25rem',
                            borderLeftColor: 'rgb(229 231 235)',
                            quotes: '"\\201C""\\201D""\\2018""\\2019"',
                        },
                        h1: {
                            color: 'inherit',
                            fontWeight: '800',
                            fontSize: '2.25em',
                            marginTop: '0',
                            marginBottom: '0.8888889em',
                            lineHeight: '1.1111111',
                        },
                        h2: {
                            color: 'inherit',
                            fontWeight: '700',
                            fontSize: '1.5em',
                            marginTop: '2em',
                            marginBottom: '1em',
                            lineHeight: '1.3333333',
                        },
                        h3: {
                            color: 'inherit',
                            fontWeight: '600',
                            fontSize: '1.25em',
                            marginTop: '1.6em',
                            marginBottom: '0.6em',
                            lineHeight: '1.6',
                        },
                        h4: {
                            color: 'inherit',
                            fontWeight: '600',
                            marginTop: '1.5em',
                            marginBottom: '0.5em',
                            lineHeight: '1.5',
                        },
                        'img, svg, video, canvas, audio, iframe, embed, object': {
                            display: 'block',
                            verticalAlign: 'middle',
                        },
                        'img, video': {
                            maxWidth: '100%',
                            height: 'auto',
                        },
                        '[hidden]': {
                            display: 'none',
                        },
                        'pre': {
                            backgroundColor: 'rgb(15 23 42)',
                            color: 'rgb(248 250 252)',
                            overflow: 'auto',
                            fontSize: '0.875em',
                            lineHeight: '1.7142857',
                            marginTop: '1.7142857em',
                            marginBottom: '1.7142857em',
                            borderRadius: '0.375rem',
                            paddingTop: '0.8571429em',
                            paddingRight: '1.1428571em',
                            paddingBottom: '0.8571429em',
                            paddingLeft: '1.1428571em',
                        },
                        'pre code': {
                            backgroundColor: 'transparent',
                            borderWidth: '0',
                            borderRadius: '0',
                            padding: '0',
                            fontWeight: 'inherit',
                            color: 'inherit',
                            fontSize: 'inherit',
                            fontFamily: 'inherit',
                            lineHeight: 'inherit',
                        },
                        'pre code::before': {
                            content: 'none',
                        },
                        'pre code::after': {
                            content: 'none',
                        },
                        code: {
                            color: 'rgb(219 39 119)',
                            fontWeight: '600',
                            fontSize: '0.875em',
                        },
                        'code::before': {
                            content: '"`"',
                        },
                        'code::after': {
                            content: '"`"',
                        },
                        'a code': {
                            color: 'inherit',
                        },
                        'h1 code': {
                            color: 'inherit',
                        },
                        'h2 code': {
                            color: 'inherit',
                            fontSize: '0.875em',
                        },
                        'h3 code': {
                            color: 'inherit',
                            fontSize: '0.9em',
                        },
                        'h4 code': {
                            color: 'inherit',
                        },
                        'blockquote code': {
                            color: 'inherit',
                        },
                        'thead th code': {
                            color: 'inherit',
                        },
                        table: {
                            width: '100%',
                            tableLayout: 'auto',
                            textAlign: 'left',
                            marginTop: '2em',
                            marginBottom: '2em',
                            fontSize: '0.875em',
                            lineHeight: '1.7142857',
                        },
                        thead: {
                            borderBottomWidth: '1px',
                            borderBottomColor: 'rgb(229 231 235)',
                        },
                        'thead th': {
                            color: 'rgb(17 24 39)',
                            fontWeight: '600',
                            verticalAlign: 'bottom',
                            paddingRight: '0.5714286em',
                            paddingBottom: '0.5714286em',
                            paddingLeft: '0.5714286em',
                        },
                        'tbody tr': {
                            borderBottomWidth: '1px',
                            borderBottomColor: 'rgb(229 231 235)',
                        },
                        'tbody tr:last-child': {
                            borderBottomWidth: '0',
                        },
                        'tbody td': {
                            verticalAlign: 'baseline',
                        },
                        tfoot: {
                            borderTopWidth: '1px',
                            borderTopColor: 'rgb(229 231 235)',
                        },
                        'tfoot td': {
                            verticalAlign: 'top',
                        },
                    },
                },
                invert: {
                    css: {
                        '--tw-prose-body': 'rgb(209 213 219)',
                        '--tw-prose-headings': 'rgb(255 255 255)',
                        '--tw-prose-lead': 'rgb(156 163 175)',
                        '--tw-prose-links': 'rgb(59 130 246)',
                        '--tw-prose-bold': 'rgb(255 255 255)',
                        '--tw-prose-counters': 'rgb(156 163 175)',
                        '--tw-prose-bullets': 'rgb(75 85 99)',
                        '--tw-prose-hr': 'rgb(55 65 81)',
                        '--tw-prose-quotes': 'rgb(156 163 175)',
                        '--tw-prose-quote-borders': 'rgb(55 65 81)',
                        '--tw-prose-captions': 'rgb(156 163 175)',
                        '--tw-prose-code': 'rgb(255 255 255)',
                        '--tw-prose-pre-code': 'rgb(209 213 219)',
                        '--tw-prose-pre-bg': 'rgb(15 23 42)',
                        '--tw-prose-th-borders': 'rgb(55 65 81)',
                        '--tw-prose-td-borders': 'rgb(55 65 81)',
                    },
                },
            },
        },
    },
    plugins: [
        require('@tailwindcss/typography'),
    ],
}
